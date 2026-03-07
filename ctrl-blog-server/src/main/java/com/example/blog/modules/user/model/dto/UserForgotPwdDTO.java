package com.example.blog.modules.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 忘记密码重置DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "忘记密码重置请求参数")
public class UserForgotPwdDTO {

    @Schema(description = "注册邮箱 (将用于接收验证码)", example = "newuser@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "邮箱验证码 (6位数字)", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须为6位数字")
    private String code;

    @Schema(description = "新密码 (8-20位，必须包含字母和数字)", example = "NewPass456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度应在8-20位之间")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[\\x21-\\x7E]{8,20}$",
            message = "密码只能包含字母、数字和英文符号"
    )
    private String newPassword;
}