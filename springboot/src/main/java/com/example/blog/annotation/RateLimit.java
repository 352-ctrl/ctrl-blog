package com.example.blog.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    String key() default "";  // 限流Key，默认按 IP 限流
    int time() default 60;    // 限流时间,单位秒
    int count() default 5;    // 限流次数 (60秒内最多5次)
}