package com.example.blog.core.config;

import com.example.blog.core.interceptor.JWTInterceptor;
import com.example.blog.core.interceptor.VisitInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 * 配置拦截器、跨域等Web相关设置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private JWTInterceptor jwtInterceptor;

    @Resource
    private VisitInterceptor visitInterceptor;

    /**
     * 添加拦截器配置
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册JWT拦截器，拦截所有请求，排除登录、注册、文件下载等接口
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/v1/auth/**",
                        "/api/v1/files/*.*",
                        "/error",
                        "/actuator",
                        "/captcha/**",
                        "/doc.html",             // 放行页面
                        "/webjars/**",           // 放行静态资源
                        "/v3/api-docs/**",       // 放行接口数据
                        "/swagger-resources/**"  // 放行兼容资源
                );

        registry.addInterceptor(visitInterceptor)
                .addPathPatterns("/api/v1/articles/**")  // 拦截前端路径
                .excludePathPatterns(
                        "/api/v1/articles/*/view",
                        "/api/v1/articles/search/index"
                );
    }

}