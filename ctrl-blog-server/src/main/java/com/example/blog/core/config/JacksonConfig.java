package com.example.blog.core.config;

import com.example.blog.common.constants.Constants;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
            // 1. 解决前端 JS 中 Long 类型精度丢失的问题
            builder.serializerByType(Long.class, ToStringSerializer.instance);

            // 2. 全局配置 LocalDateTime 的序列化（后端 -> 前端）
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.FORMAT_DATETIME);
            builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(formatter));

            // 3. 全局配置 LocalDateTime 的反序列化（前端 -> 后端）
            builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        };
    }
}