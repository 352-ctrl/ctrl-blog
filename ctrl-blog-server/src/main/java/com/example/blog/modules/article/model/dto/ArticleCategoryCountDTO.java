package com.example.blog.modules.article.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章分类统计 DTO
 * 用于 Service 层向 Manager 层传输聚合统计数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文章分类统计结果")
public class ArticleCategoryCountDTO {

    @Schema(description = "分类ID", example = "1623456789012345678")
    private Long categoryId;

    @Schema(description = "分类名称", example = "后端技术")
    private String categoryName;

    @Schema(description = "该分类下的文章数量", example = "10")
    private Long count;

}
