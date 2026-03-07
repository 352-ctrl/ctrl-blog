package com.example.blog.modules.operation.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 新增评论DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "新增评论请求参数")
public class CommentAddDTO {

    @Schema(description = "文章ID", example = "1623456789012345678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "文章ID不能为空")
    @Positive(message = "文章ID必须为正整数")
    private Long articleId;

    @Schema(description = "父评论ID (0或不传代表顶级评论，否则为回复)", example = "1623456789012345678", defaultValue = "0")
    @PositiveOrZero(message = "父评论ID必须为非负整数")
    private Long parentId;

    @Schema(description = "评论内容", example = "这篇文章写得很好！", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容长度不能超过500字")
    private String content;

}