package com.example.blog.modules.system.validator;

import cn.hutool.core.util.StrUtil;
import com.example.blog.core.annotation.CheckSensitive;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

/**
 * 敏感词校验执行器
 */
@Component
public class SensitiveWordValidator implements ConstraintValidator<CheckSensitive, String> {

    @Resource
    private SensitiveWordManager sensitiveWordManager;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 如果字段为空，放行（判空由 @NotBlank 负责，这里只管敏感词）
        if (StrUtil.isBlank(value)) {
            return true;
        }

        // 核心逻辑：如果不包含敏感词，返回 true (校验通过)；否则返回 false (校验失败)
        return !sensitiveWordManager.contains(value);
    }
}
