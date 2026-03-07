package com.example.blog.core.annotation;

import com.example.blog.common.constants.MessageConstants;
import com.example.blog.modules.system.validator.SensitiveWordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 敏感词校验注解
 * 配合 @Valid / @Validated 使用
 */
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Documented
// 指定真正执行校验逻辑的类
@Constraint(validatedBy = SensitiveWordValidator.class)
public @interface CheckSensitive {

    // 校验失败时的默认错误提示（引入你刚才配置的常量）
    String message() default MessageConstants.MSG_CONTENT_SENSITIVE;

    // 分组校验使用（必须有）
    Class<?>[] groups() default {};

    // 负载参数（必须有）
    Class<? extends Payload>[] payload() default {};
}
