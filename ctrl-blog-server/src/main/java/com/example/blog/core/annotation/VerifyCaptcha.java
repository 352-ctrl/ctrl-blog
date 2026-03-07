package com.example.blog.core.annotation;

import java.lang.annotation.*;

/**
 * 滑动验证码校验注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VerifyCaptcha {
}