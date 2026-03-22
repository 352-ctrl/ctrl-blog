package com.example.blog.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口文档配置类
 * <p>
 * 基于 Knif4j/SpringDoc，配置 OpenAPI 文档的基本信息
 * 访问地址：http://localhost:8080/doc.html
 */
@Configuration
public class Knif4jConfig {

    /**
     * 配置 OpenAPI 元数据
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CtrlBlog 博客系统后端API接口文档")      // 文档标题
                        .version("1.0")              // 版本号
                        .description("CtrlBlog 是基于 Spring Boot 3 + Vue 3 开发的前后端分离现代化博客系统，该文档包含所有后端核心业务接口（文章管理、用户交互、系统监控等）的定义、请求参数及返回值说明。")  // 描述
                        .contact(new Contact().name("ctrl")) // 作者信息
                );
    }
}