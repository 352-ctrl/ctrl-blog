package com.example.blog.core.aspect;

import cn.hutool.core.util.StrUtil;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.core.exception.CustomerException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 滑动验证码统一校验切面
 */
@Slf4j
@Aspect
@Component
public class CaptchaAspect {

    @Resource
    private CaptchaService captchaService;

    // 1. 明确的启用开关，默认绝对关闭 (false)
    @Value("${aj.captcha.bypass.enabled:false}")
    private boolean bypassEnabled;

    // 2. 万能验证码，默认空
    @Value("${aj.captcha.bypass.master-key:}")
    private String masterKey;

    /**
     * 拦截所有标注了 @VerifyCaptcha 注解的方法
     */
    @Before("@annotation(com.example.blog.core.annotation.VerifyCaptcha)")
    public void doBefore(JoinPoint joinPoint) {
        String captchaVerification = getCaptchaVerification(joinPoint);

        log.info("【验证码切面】接收到的 captchaVerification: {}", captchaVerification);

        // 1. 校验是否为空
        if (StrUtil.isBlank(captchaVerification)) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_CAPTCHA_REQUIRE);
        }

        // 如果请求传来的验证码等于配置的万能验证码，直接放行！
        if (bypassEnabled && StrUtil.isNotBlank(masterKey) && masterKey.equals(captchaVerification)) {
            log.warn("【安全警告】当前环境已开启验证码 Bypass 后门，跳过滑动验证！");
            return;
        }

        // 2. 调用 Anji-Captcha 组件进行二次校验
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaVerification(captchaVerification);
        ResponseModel response = captchaService.verification(captchaVO);

        // 3. 校验失败，抛出业务异常阻断请求
        if (!response.isSuccess()) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_CAPTCHA_VERIFY_FAILED);
        }
    }

    /**
     * 核心：三级参数降级寻找策略
     * 尝试从 Header -> Parameter 中提取 captchaVerification
     */
    private String getCaptchaVerification(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // 级别 1：尝试从 HTTP Header 中获取
            String verification = request.getHeader("captchaVerification");
            if (StrUtil.isNotBlank(verification)) {
                return verification;
            }

            // 级别 2：尝试从 URL Query Parameter 或 Form 表单中获取
            verification = request.getParameter("captchaVerification");
            if (StrUtil.isNotBlank(verification)) {
                return verification;
            }
        }

        // 级别 3：尝试从被拦截方法的参数(如 DTO)中通过反射获取 (解决 @RequestBody JSON 数据的问题)
        for (Object arg : joinPoint.getArgs()) {
            if (arg != null) {
                try {
                    Method method = arg.getClass().getMethod("getCaptchaVerification");
                    Object value = method.invoke(arg);
                    if (value != null && StrUtil.isNotBlank(value.toString())) {
                        return value.toString();
                    }
                } catch (Exception e) {
                    // 参数对象没有 getCaptchaVerification 方法，静默忽略，检查下一个参数
                }
            }
        }

        return null;
    }
}