package com.example.blog.core.aspect;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.blog.core.annotation.Log;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.modules.user.model.dto.UserPayloadDTO;
import com.example.blog.modules.system.model.entity.SysOperLog;
import com.example.blog.modules.system.service.SysOperLogService;
import com.example.blog.common.utils.IpUtils;
import com.example.blog.core.security.UserContext;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 操作日志切面
 * 使用 AOP 拦截带有 @Log 注解的方法，并异步记录到数据库
 */
@Aspect
@Component
@Slf4j
public class LogAspect {

    @Resource
    private SysOperLogService sysOperLogService;

    /**
     * 环绕通知
     */
    @Around("@annotation(controllerLog)")
    public Object doAround(ProceedingJoinPoint joinPoint, Log controllerLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 1. 执行目标方法
        Object result = null;
        Exception exception = null;
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            exception = e;
            throw e; // 异常必须抛出去，不能吞掉，否则前端收不到错误提示
        } finally {
            Integer costTime = Math.toIntExact(System.currentTimeMillis() - startTime);
            // 1. 在主线程中提前抓取 Request 数据（RequestContextHolder 无法跨线程传递）
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

            // 提取关键信息 (String 是线程安全的)
            String ip = request != null ? IpUtils.getIpAddr(request) : Constants.UNKNOWN;
            String userAgent = request != null ? request.getHeader(Constants.HEADER_USER_AGENT) : StrUtil.EMPTY;
            String requestMethod = request != null ? request.getMethod() : StrUtil.EMPTY;

            // 提取需要异步处理的数据
            saveLogAsync(joinPoint, controllerLog, result, exception, costTime, ip, userAgent, requestMethod);
        }

        return result;
    }

    /**
     * 保存日志 (建议做成异步，不影响主业务性能)
     */
    private void saveLogAsync(ProceedingJoinPoint joinPoint, Log controllerLog, Object result, Exception e, Integer costTime, String ip, String userAgent, String requestMethod) {
        // 获取当前用户（主线程获取）
        UserPayloadDTO currentUser = UserContext.get();

        // 提取类名信息
        String methodName = joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName();
        // 参数脱敏处理
        String params = maskSensitiveData(joinPoint.getArgs());

        // 如果用户未登录（currentUser为空），尝试从 Result 中补全用户信息
        // 场景：登录接口、注册接口
        if (currentUser == null && result != null) {
            currentUser = tryGetUserInfoFromResult(result);
        }

        // 为了在异步线程中使用，需要由 final 变量承接
        UserPayloadDTO finalUser = currentUser;

        // 2. 异步执行数据库写入
        CompletableFuture.runAsync(() -> {
            try {
                SysOperLog sysOperLog = new SysOperLog();
                sysOperLog.setModule(controllerLog.module());
                sysOperLog.setRequestMethod(requestMethod);
                sysOperLog.setType(controllerLog.type());
                sysOperLog.setDescription(controllerLog.desc());
                sysOperLog.setIp(ip);
                // 设置 UserAgent，记得截断防止数据库报错
                if (userAgent != null && userAgent.length() > 500) {
                    sysOperLog.setUserAgent(userAgent.substring(0, 500));
                } else {
                    sysOperLog.setUserAgent(userAgent);
                }
                sysOperLog.setMethod(methodName);
                sysOperLog.setParams(StrUtil.sub(params, 0, Constants.SYS_LOG_MAX_LENGTH));

                if (finalUser != null) {
                    sysOperLog.setUserId(finalUser.getId());
                    sysOperLog.setNickname(finalUser.getNickname());
                } else {
                    sysOperLog.setNickname(MessageConstants.MSG_LOG_GUEST);
                }

                if (e != null) {
                    sysOperLog.setStatus(BizStatus.Log.FAIL.getValue());
                    sysOperLog.setResult(StrUtil.sub(e.toString(), 0, Constants.SYS_LOG_MAX_LENGTH));
                } else {
                    sysOperLog.setStatus(BizStatus.Log.SUCCESS.getValue());
                    sysOperLog.setResult(MessageConstants.MSG_SUCCESS);
                }

                sysOperLog.setCostTime(costTime);
                sysOperLog.setCreateTime(LocalDateTime.now());

                sysOperLogService.addLog(sysOperLog);
            } catch (Exception ex) {
                log.error(MessageConstants.MSG_LOG_ERROR, ex);
            }
        });
    }

    /**
     * 参数脱敏：将 password 等敏感字段替换为 ******
     */
    private String maskSensitiveData(Object[] args) {
        if (args == null || args.length == 0) {
            return StrUtil.EMPTY;
        }

        try {
            List<Object> safeArgs = new ArrayList<>();

            for (Object arg : args) {
                // 1. 处理文件上传对象
                if (arg instanceof MultipartFile file) {
                    Map<String, String> fileMap = new HashMap<>();
                    fileMap.put("fileName", file.getOriginalFilename());
                    fileMap.put("fileSize", String.valueOf(file.getSize()));
                    fileMap.put("contentType", file.getContentType());
                    safeArgs.add(fileMap);
                    continue;
                }

                // 2. 过滤掉 Request / Response / BindingResult 等对象
                if (arg instanceof HttpServletRequest
                        || arg instanceof HttpServletResponse
                        || arg instanceof BindingResult) {
                    continue;
                }

                // 3. 其他普通参数直接添加
                safeArgs.add(arg);
            }

            // 将参数数组转换为 JSON 数组
            JSONArray jsonArray = JSONUtil.parseArray(safeArgs);

            for (Object obj : jsonArray) {
                if (obj instanceof JSONObject jsonObject) {
                    // 1. 邮箱脱敏 (使用你定义的正则常量)
                    if (jsonObject.containsKey("email")) {
                        String email = jsonObject.getStr("email");
                        if (StrUtil.isNotBlank(email)) {
                            // 利用DesensitizedUtil替换
                            jsonObject.set("email", DesensitizedUtil.email(email));
                        }
                    }

                    // 2. 密码脱敏 (遍历常量数组，避免写死 key)
                    for (String key : Constants.SENSITIVE_KEYS) {
                        if (jsonObject.containsKey(key)) {
                            jsonObject.set(key, Constants.MASK_PASSWORD);
                        }
                    }
                }
            }
            return jsonArray.toString();
        } catch (Exception e) {
            // 如果转换失败（比如参数不是 JSON 格式），则简单返回 toString，但最好还是尽量脱敏
            return StrUtil.sub(JSONUtil.toJsonStr(args), 0, Constants.SYS_LOG_MAX_LENGTH);
        }
    }

    /**
     * 尝试从返回值 Result 中提取用户信息
     * 适用于：登录成功后，从返回值里拿用户信息记录日志
     */
    private UserPayloadDTO tryGetUserInfoFromResult(Object result) {
        try {
            // 将 result 转为 JSON 对象
            JSONObject jsonResult = JSONUtil.parseObj(result);
            // 判断 code 是否为 200
            if (ResultCode.SUCCESS.equals(jsonResult.getStr("code"))) {
                JSONObject data = jsonResult.getJSONObject("data");
                if (data != null) {
                    JSONObject userInfo = data.getJSONObject("userInfo");
                    // 如果 userInfo 为空，尝试直接从 data 里取 (防止结构不一致)
                    if (userInfo == null && data.containsKey("id") && data.containsKey("nickname")) {
                        userInfo = data;
                    }

                    if (userInfo != null) {
                        String roleStr = userInfo.getStr("role");
                        BizStatus.Role roleEnum = null;
                        if (roleStr != null) {
                            roleEnum = BizStatus.Role.valueOf(roleStr);
                        }
                        return UserPayloadDTO.builder()
                                .id(userInfo.getLong("id"))
                                .nickname(userInfo.getStr("nickname"))
                                .role(roleEnum) // 如果需要记录角色
                                .build();
                    }
                }
            }
        } catch (Exception e) {
            // 解析失败忽略，保持 currentUser 为 null
        }
        return null;
    }
}