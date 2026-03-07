package com.example.blog.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域资源共享配置
 * 配置允许跨域请求的规则，解决前端跨域访问问题
 */
@Configuration
public class CorsConfig {

    /**
     * 注册CORS过滤器
     *
     * @return CorsFilter跨域过滤器实例
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // 允许所有域名访问，生产环境建议指定具体域名
        corsConfiguration.addAllowedOrigin("*");

        // 允许所有请求头
        corsConfiguration.addAllowedHeader("*");

        // 允许所有HTTP方法
        corsConfiguration.addAllowedMethod("*");

        // 对所有接口应用跨域配置
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(source);
    }
}