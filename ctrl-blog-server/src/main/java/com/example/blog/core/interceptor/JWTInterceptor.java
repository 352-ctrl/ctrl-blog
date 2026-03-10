package com.example.blog.core.interceptor;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.common.utils.RedisUtil;
import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.core.exception.CustomerException;
import com.example.blog.core.security.TokenUtils;
import com.example.blog.core.security.UserContext;
import com.example.blog.modules.user.model.dto.UserPayloadDTO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT令牌验证拦截器
 * 拦截HTTP请求，验证JWT令牌的有效性和权限
 */
@Component
public class JWTInterceptor implements HandlerInterceptor {

    @Resource
    private RedisUtil redisUtil;

    /**
     * 前置处理方法，验证JWT令牌
     *
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param handler 处理器对象
     * @return 验证通过返回true，否则抛出异常
     * @throws Exception 验证失败时抛出异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求 (CORS 预检)
        // 浏览器发送的预检请求不带 Token，必须放行，否则前端会报跨域错误
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 如果不是映射到 Controller 方法（例如是静态资源），直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 解析 Token
        String token = null;

        // 1. 优先尝试获取标准的 Authorization Header
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith(Constants.TOKEN_PREFIX)) {
            token = bearerToken.substring(Constants.TOKEN_PREFIX.length()).trim();
        }

        // 2. 降级策略 1：尝试获取自定义 Header
        if (StrUtil.isBlank(token)) {
            token = request.getHeader(Constants.HEADER_TOKEN);
        }

        // 3. 降级策略 2：尝试从 URL 参数中获取 (常用于文件下载、WebSocket握手等场景)
        if (StrUtil.isBlank(token)) {
            token = request.getParameter(Constants.HEADER_TOKEN);
        }

        // 判断是否必须登录 (检查方法或类上是否有 @AuthCheck 注解)
        AuthCheck authCheck = handlerMethod.getMethodAnnotation(AuthCheck.class);
        if (authCheck == null) {
            // 如果方法上没有，再看类上有没有
            authCheck = handlerMethod.getBeanType().getAnnotation(AuthCheck.class);
        }
        boolean isAuthRequired = (authCheck != null);

        // 验证token是否存在
        if (StrUtil.isBlank(token)) {
            if (isAuthRequired) {
                throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
            }
            return true;
        }

        try {
            DecodedJWT jwt = TokenUtils.verify(token);
            String role = jwt.getClaim(Constants.CLAIM_ROLE).asString();
            Long userId = jwt.getClaim(Constants.CLAIM_ID).asLong();
            String nickname = jwt.getClaim(Constants.CLAIM_NICKNAME).asString();

            BizStatus.Role roleEnum;
            try {
                roleEnum = BizStatus.Role.valueOf(role);
            } catch (Exception e) {
                // 如果转换失败（比如存了未知角色），抛出异常或给个默认值
                throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_ROLE_INVALID);
            }

            // 检查 Redis 中该用户的在线 Token 记录是否存在
            String redisTokenKey = RedisConstants.REDIS_USER_TOKEN_KEY + userId;
            Object storedTokenObj = redisUtil.get(redisTokenKey);

            // 检查当前 Token 是否在黑名单中 (处理重置密码/退出登录的黑名单逻辑)
            String blacklistKey = RedisConstants.REDIS_TOKEN_BLACKLIST_PREFIX + token;
            boolean isBlacklisted = redisUtil.hasKey(blacklistKey);

            // 如果满足以下任意条件，判定为失效：
            // - 情况A: Redis 在线记录已消失 (可能被重置密码删除了)
            // - 情况B: 当前 Token 在黑名单中 (可能用户已点退出或已被踢出)
            if (storedTokenObj == null || isBlacklisted) {
                if (isAuthRequired) {
                    throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_SESSION_INVALID);
                }
                // 如果不是必须登录的接口，清空 context 走游客模式
                UserContext.remove();
                return true;
            }

            // 将当前用户ID和权限放入 ThreadLocal
            UserContext.set(new UserPayloadDTO(userId, roleEnum, nickname));
        } catch (CustomerException ce) {
            // 1. 如果主动抛出的业务异常（比如角色无效），直接原样往外抛
            throw ce;
        } catch (Exception e) {
            // 2. 如果是 JWT 库抛出的解析异常（伪造、过期等），才统一报 Token 无效
            if (isAuthRequired) {
                throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_TOKEN_INVALID);
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.remove();
    }
}