package com.example.blog.modules.article.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "极简版文章列表展示对象 (VO)", title = "ArticleSimpleVO")
public class ArticleSimpleVO {

    @Schema(description = "文章ID", type = "string", example = "1623456789012345678")
    private Long id;

    @Schema(description = "文章封面URL", example = "https://example.com/cover/1.jpg")
    private String cover;

    @Schema(description = "文章标题", example = "Spring Boot 3.0 核心特性解析")
    private String title;

    @Schema(
            description = "发布时间",
            example = "2023-10-24 10:24:00",
            type = "string"
    )
    private LocalDateTime createTime;

}