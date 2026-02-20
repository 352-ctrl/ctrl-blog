package com.example.blog.common.jackson;

import cn.hutool.core.util.DesensitizedUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * 邮箱脱敏 JSON 序列化器
 */
public class EmailDesensitizeSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String email, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (StringUtils.hasText(email)) {
            // 使用 Hutool 自带的邮箱脱敏 (例如: 123456@qq.com -> 123****@qq.com)
            gen.writeString(DesensitizedUtil.email(email));
        } else {
            gen.writeNull();
        }
    }
}
