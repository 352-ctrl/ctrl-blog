package com.example.blog.aspect;

import cn.hutool.core.util.StrUtil;
import com.example.blog.annotation.AuthCheck;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.dto.user.UserPayloadDTO;
import com.example.blog.exception.CustomerException;
import com.example.blog.utils.UserContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 权限校验切面
 */
@Aspect
@Component
public class AuthAspect {

    @Around("@annotation(com.example.blog.annotation.AuthCheck) || @within(com.example.blog.annotation.AuthCheck)")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {

        // 1. 获取权限注解（优先取方法上的，如果方法上没有，就取类上的）
        AuthCheck authCheck = getAuthCheckAnnotation(joinPoint);
        // 如果没有注解，直接放行
        if (authCheck == null) {
            return joinPoint.proceed();
        }

        // 2. 获取当前登录用户
        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        // 3. 校验角色
        String mustRole = authCheck.role();

        // 情况 A：注解没有指定角色（默认为空），说明只要求登录 -> 直接放行
        if (StrUtil.isBlank(mustRole)) {
            return joinPoint.proceed();
        }

        // 情况 B：当前用户是 ADMIN -> 超级管理员，无视角色限制，直接放行
        if (currentUser.getRole() == BizStatus.Role.ADMIN) {
            return joinPoint.proceed();
        }

        // 获取枚举里的值 (例如 "ADMIN")
        String currentRoleCode = currentUser.getRole().getValue();

        // 情况 C：当前用户不是 ADMIN且注解要求了ADMIN
        if (!mustRole.equals(currentRoleCode)) {
            throw new CustomerException(ResultCode.FORBIDDEN, MessageConstants.MSG_NO_PERMISSION);
        }

        return joinPoint.proceed();
    }

    /**
     * 辅助方法：获取注解
     * 优先级：方法上的 > 类上的
     */
    private AuthCheck getAuthCheckAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 1. 先尝试获取方法上的注解
        AuthCheck methodAnnotation = method.getAnnotation(AuthCheck.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        // 2. 如果方法上没有，再获取类上的注解
        Class<?> targetClass = joinPoint.getTarget().getClass();
        return targetClass.getAnnotation(AuthCheck.class);
    }
}
