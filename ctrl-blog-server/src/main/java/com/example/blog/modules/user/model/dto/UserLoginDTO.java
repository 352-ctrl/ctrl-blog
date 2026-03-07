package com.example.blog.modules.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登录请求参数")
public class UserLoginDTO {

    @Schema(description = "登录邮箱", example = "admin@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    @NotBlank(message = "登录邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "登录密码", example = "Password123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "登录密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度应在8-20位之间")
    private String password;

    @NotBlank(message = "请先完成安全验证")
    private String captchaVerification;

}