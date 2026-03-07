package com.example.blog.core.annotation;

import cn.hutool.core.util.StrUtil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
    String module(); // 模块，例：文章管理
    String type();   // 操作类型，例：新增、修改、删除
    String desc() default StrUtil.EMPTY; // 描述
}