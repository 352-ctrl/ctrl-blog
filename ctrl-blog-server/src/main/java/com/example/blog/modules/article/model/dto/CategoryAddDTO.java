package com.example.blog.modules.article.model.dto;

import com.example.blog.core.annotation.CheckSensitive;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 新增分类DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "新增分类请求参数")
public class CategoryAddDTO {

    @Schema(description = "分类名称", example = "后端技术", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 20, message = "分类名称长度不能超过20个字符")
    @CheckSensitive(message = "分类名称包含违规词汇，请修改")
    private String name;

}