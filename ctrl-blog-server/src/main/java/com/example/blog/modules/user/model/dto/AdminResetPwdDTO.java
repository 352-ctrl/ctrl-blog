package com.example.blog.modules.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员重置用户密码DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员重置密码请求参数")
public class AdminResetPwdDTO {

    @Schema(description = "用户ID", example = "1623456789012345678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    @Positive(message = "用户ID必须为正整数")
    private Long id;

    @Schema(description = "重置后的新密码 (8-20位，必须包含字母和数字)", example = "DefaultPass123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度应在8-20位之间")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[\\x21-\\x7E]{8,20}$",
            message = "密码只能包含字母、数字和英文符号"
    )
    private String newPassword;
}