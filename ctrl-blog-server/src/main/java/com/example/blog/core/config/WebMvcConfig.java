package com.example.blog.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-path}")
    private String basePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保 basePath 是以 / 结尾的，比如 D:/blog/files/
        String absolutePath = "file:" + basePath;

        // 映射前台请求到本地硬盘目录
        registry.addResourceHandler("/avatar/**")
                .addResourceLocations(absolutePath + "avatar/");

        registry.addResourceHandler("/cover/**")
                .addResourceLocations(absolutePath + "cover/");

        registry.addResourceHandler("/article/**")
                .addResourceLocations(absolutePath + "article/");
    }
}
