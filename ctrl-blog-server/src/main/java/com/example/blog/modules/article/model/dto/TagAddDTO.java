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
 * 新增标签DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "新增标签请求参数")
public class TagAddDTO {

    @Schema(description = "标签名称", example = "Spring Boot", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 20, message = "标签名称长度不能超过20个字符")
    @CheckSensitive(message = "标签名称包含违规词汇，请修改")
    private String name;

}