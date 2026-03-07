package com.example.blog.modules.operation.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮箱请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "邮箱相关请求参数 (如发送验证码)")
public class EmailRequestDTO {

    @Schema(description = "邮箱地址", example = "test@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

}