package com.example.blog.core.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 全局序列化配置类
 * <p>
 * 主要用于解决前后端交互时，Java Long 类型 (雪花算法 ID) 在前端 JavaScript 中精度丢失的问题。
 * 配置后，所有的 Long 类型字段在返回 JSON 时都会自动转换为 String 类型。
 */
@Configuration
public class JacksonConfig {

    /**
     * 自定义 Jackson 序列化构建器
     * 指定 Long 类型统一使用 String 序列化
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // 将 Long 类型序列化为 String
            builder.serializerByType(Long.class, ToStringSerializer.instance);
        };
    }
}