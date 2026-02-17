package com.example.blog.interceptor;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.blog.annotation.AuthCheck;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.dto.user.UserPayloadDTO;
import com.example.blog.exception.CustomerException;
import com.example.blog.utils.TokenUtils;
import com.example.blog.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT令牌验证拦截器
 * 拦截HTTP请求，验证JWT令牌的有效性和权限
 */
@Component
public class JWTInterceptor implements HandlerInterceptor {

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

        // 从请求头获取token
        String token = request.getHeader(Constants.HEADER_TOKEN);

        if (StrUtil.isEmpty(token)) {
            // 如果请求头中没有，尝试从请求参数中获取
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

            // 将当前用户ID和权限放入 ThreadLocal
            UserContext.set(new UserPayloadDTO(userId, roleEnum, nickname));
        } catch (Exception e) {
            // 验证失败进入这里
            if (isAuthRequired) {
                throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_TOKEN_INVALID);
            }
            // 非必须登录接口，忽略异常，直接放行（游客模式）
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.remove();
    }
}