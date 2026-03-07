package com.example.blog.core.aspect;

import cn.hutool.core.util.StrUtil;
import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.modules.user.model.dto.UserPayloadDTO;
import com.example.blog.core.exception.CustomerException;
import com.example.blog.core.security.UserContext;
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

    @Around("@annotation(com.example.blog.core.annotation.AuthCheck) || @within(com.example.blog.core.annotation.AuthCheck)")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {

        // 1. 获取权限注解（方法级别优先于类级别）
        AuthCheck authCheck = getAuthCheckAnnotation(joinPoint);
        if (authCheck == null) {
            return joinPoint.proceed(); // 无注解直接放行（理论上不会走到这里，作为安全兜底）
        }

        // 2. 校验用户是否已登录
        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        // 3. 获取接口要求的角色
        String mustRole = authCheck.role();

        // 4. 若注解未指定具体角色，说明仅需登录即可访问，直接放行
        if (StrUtil.isBlank(mustRole)) {
            return joinPoint.proceed();
        }

        // 5. 获取当前用户的角色信息
        BizStatus.Role currentRole = currentUser.getRole();
        if (currentRole == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, "用户角色状态异常，请重新登录");
        }

        // ================= 层级权限校验核心逻辑 =================

        // 级别 1：当前用户是【超级管理员】，拥有最高特权，无视任何限制直接放行
        if (currentRole == BizStatus.Role.SUPER_ADMIN) {
            return joinPoint.proceed();
        }

        // 级别 2：当前用户是【普通管理员】
        if (currentRole == BizStatus.Role.ADMIN) {
            // 禁止越权：管理员不能访问超级管理员专属接口
            if (BizStatus.ROLE_SUPER_ADMIN.equals(mustRole)) {
                throw new CustomerException(ResultCode.FORBIDDEN, MessageConstants.MSG_NO_PERMISSION);
            }
            // 向下兼容：访问管理员或普通用户的接口，予以放行
            return joinPoint.proceed();
        }

        // 级别 3：当前用户是【普通用户】
        // 必须严格匹配接口要求的角色 (即只能访问未指定role或明确标为USER的接口)
        String currentRoleCode = currentRole.getValue();
        if (!mustRole.equals(currentRoleCode)) {
            throw new CustomerException(ResultCode.FORBIDDEN, MessageConstants.MSG_NO_PERMISSION);
        }

        // 校验通过，放行目标方法
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
