package com.example.blog.core.annotation;

import cn.hutool.core.util.StrUtil;

import java.lang.annotation.*;

/**
 * 权限校验注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthCheck {
    // 默认不限制特定角色
    String role() default StrUtil.EMPTY;
}
