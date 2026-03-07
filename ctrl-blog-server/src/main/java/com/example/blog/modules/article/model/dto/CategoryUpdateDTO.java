package com.example.blog.modules.article.model.dto;

import com.example.blog.core.annotation.CheckSensitive;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新分类DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新分类请求参数")
public class CategoryUpdateDTO {

    @Schema(description = "分类ID", example = "1623456789012345678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "分类ID不能为空")
    @Positive(message = "分类ID必须为正整数")
    private Long id;

    @Schema(description = "分类名称", example = "后端技术", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 20, message = "分类名称长度不能超过20个字符")
    @CheckSensitive(message = "分类名称包含违规词汇，请修改")
    private String name;

}