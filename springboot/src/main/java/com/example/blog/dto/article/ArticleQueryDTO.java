package com.example.blog.dto.article;

import com.example.blog.dto.PageQueryDTO;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
    @JsonSerialize(contentUsing = ToStringSerializer.class)
    private List<@Positive(message = "标签ID必须为正整数") Long> tagIds;

}