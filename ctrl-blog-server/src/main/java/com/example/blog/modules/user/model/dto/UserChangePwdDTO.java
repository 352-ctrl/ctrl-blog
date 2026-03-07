package com.example.blog.modules.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户修改密码DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户修改密码请求参数")
public class UserChangePwdDTO {

    @Schema(description = "旧密码", example = "OldPass123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "旧密码不能为空")
    @Size(min = 8, max = 20, message = "旧密码长度应在8-20位之间")
    private String oldPassword;

    @Schema(description = "新密码 (8-20位，必须包含字母和数字)", example = "NewPass456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度应在8-20位之间")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[\\x21-\\x7E]{8,20}$",
            message = "密码只能包含字母、数字和英文符号"
    )
    private String newPassword;

}