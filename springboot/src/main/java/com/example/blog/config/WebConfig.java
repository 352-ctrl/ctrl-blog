package com.example.blog.config;

import com.example.blog.interceptor.JWTInterceptor;
import com.example.blog.interceptor.VisitInterceptor;
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
                        "/api/auth/**",
                        "/api/files/download/**",
                        "/error",
                        "/doc.html",             // 放行页面
                        "/webjars/**",           // 放行静态资源
                        "/v3/api-docs/**",       // 放行接口数据
                        "/swagger-resources/**"  // 放行兼容资源
                );

        registry.addInterceptor(visitInterceptor)
                .addPathPatterns("/api/front/articles/**")  // 拦截前端路径
                .excludePathPatterns(
                        "/api/front/articles/*/view",
                        "/api/front/articles/search/index"
                );
    }

}