package com.example.blog.modules.system.model.dto;

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
 * 更新敏感词DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新敏感词请求参数")
public class SysSensitiveWordUpdateDTO {

    @Schema(description = "敏感词ID", example = "1623456789012345678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "敏感词ID不能为空")
    @Positive(message = "敏感词ID必须为正整数")
    private Long id;

    @Schema(description = "敏感词名称", example = "雄烯二醇", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "敏感词名称不能为空")
    @Size(max = 255, message = "敏感词名称长度不能超过255个字符")
    private String word;
    
}
