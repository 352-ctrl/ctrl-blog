package com.example.blog.modules.article.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "首页轮播图展示对象", title = "ArticleCarouselVO")
public class ArticleCarouselVO {

    @Schema(description = "文章ID (点击跳转用)", type = "string", example = "1623456789012345678")
    private Long id;

    @Schema(description = "文章封面 (大图)", example = "https://example.com/cover/banner.jpg")
    private String cover;

    @Schema(description = "文章标题", example = "Spring Boot 3.0 深度解析")
    private String title;

}