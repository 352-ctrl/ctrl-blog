package com.example.blog.modules.system.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 新增敏感词DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "新增敏感词请求参数")
public class SysSensitiveWordAddDTO {

    @Schema(description = "敏感词", example = "雄烯二醇", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "敏感词名称不能为空")
    @Size(max = 255, message = "敏感词名称长度不能超过255个字符")
    private String word;

}
