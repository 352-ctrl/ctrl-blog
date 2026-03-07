package com.example.blog.core.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis缓存配置类
 * <p>
 * 1. 配置 RedisTemplate 的序列化规则（解决乱码问题）
 * 2. 配置 CacheManager 以支持 @Cacheable 注解的 JSON 序列化
 */
@Configuration
public class RedisConfig {

    /**
     * 配置 RedisTemplate (用于手动操作 redisUtil)
     * 设置 Key 为 String 序列化，Value 为 JSON 序列化
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用通用 Jackson 序列化器
        GenericJackson2JsonRedisSerializer serializer = createSerializer();

        // Key 采用 String 序列化
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value 采用 JSON 序列化
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置 CacheManager (用于 @Cacheable 注解)
     * 确保注解缓存也使用 JSON 格式，且能处理 TTL
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        GenericJackson2JsonRedisSerializer serializer = createSerializer();

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 设置 Key 的序列化
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // 设置 Value 的序列化
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                // 默认过期时间 30 分钟 (覆盖 yml 配置)
                .entryTtl(Duration.ofMinutes(30))
                // 不缓存 Null 值
                .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * 提取通用的序列化配置
     * 解决 LocalDateTime 序列化问题以及类型识别问题
     */
    private GenericJackson2JsonRedisSerializer createSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 支持 Java8 时间模块
        objectMapper.registerModule(new JavaTimeModule());
        // 允许序列化私有字段，确保反序列化成功
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 存储类信息，这样反序列化时才知道是哪个 DTO 类 (避免 LinkedHashMap 转换异常)
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}