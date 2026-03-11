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
                        .title("博客系统后端 API")      // 文档标题
                        .version("1.0")              // 版本号
                        .description("基于 Spring Boot 3 的接口文档") // 描述
                        .contact(new Contact().name("编码助手")) // 作者信息
                );
    }
}