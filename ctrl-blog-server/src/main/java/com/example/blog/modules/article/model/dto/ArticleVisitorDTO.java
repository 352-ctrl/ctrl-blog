package com.example.blog.modules.article.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章访问者DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文章访问统计参数")
public class ArticleVisitorDTO {

    @Schema(description = "文章ID", example = "1623456789012345678")
    private Long articleId;

    @Schema(description = "访客IP地址", example = "127.0.0.1")
    private String ip;

    @Schema(description = "用户代理 (User-Agent)", example = "Mozilla/5.0...")
    private String userAgent;
}
