package com.example.blog.modules.article.model.dto;

import com.example.blog.common.base.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 *  * 查询文章DTO
 *  */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文章分页查询条件")
public class ArticleQueryDTO extends PageQueryDTO {

    @Schema(description = "文章标题 (模糊查询)", example = "Spring")
    private String title;

    @Schema(description = "分类ID (用于筛选)", example = "1623456789012345678")
    private Long categoryId;

    @Schema(description = "标签ID列表 (用于筛选，查找包含指定标签的文章)", example = "[\"1623456789012345678\", \"1623456789012345679\"]")
    private List<@Positive(message = "标签ID必须为正整数") Long> tagIds;

    @Schema(description = "文章状态(0:草稿, 1:发布)")
    private Integer status;

    @Schema(description = "是否置顶(0:否, 1:是)")
    private Integer isTop;

}