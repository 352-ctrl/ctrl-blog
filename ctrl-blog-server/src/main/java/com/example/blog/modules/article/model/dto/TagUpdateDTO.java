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
 * 更新标签DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新标签请求参数")
public class TagUpdateDTO {

    @Schema(description = "标签ID", example = "1623456789012345678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "标签ID不能为空")
    @Positive(message = "标签ID必须为正整数")
    private Long id;

    @Schema(description = "标签名称", example = "Redis", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 20, message = "标签名称长度不能超过20个字符")
    @CheckSensitive(message = "标签名称包含违规词汇，请修改")
    private String name;

}