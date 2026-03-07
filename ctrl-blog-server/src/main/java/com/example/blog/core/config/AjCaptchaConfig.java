package com.example.blog.core.config;

import com.anji.captcha.config.AjCaptchaServiceAutoConfiguration;
import com.anji.captcha.config.AjCaptchaStorageAutoConfiguration;
import com.anji.captcha.properties.AjCaptchaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 解决 Spring Boot 3 无法自动装配 AJ-Captcha 的问题
 */
@Configuration
@EnableConfigurationProperties(AjCaptchaProperties.class)
@Import({
        AjCaptchaServiceAutoConfiguration.class,
        AjCaptchaStorageAutoConfiguration.class // 必须包含此项，用于将 Cache 注册到工厂
})
public class AjCaptchaConfig {
}