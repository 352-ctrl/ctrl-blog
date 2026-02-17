package com.example.blog.dto.user;

import com.example.blog.common.constants.Constants;
import com.example.blog.common.enums.BizStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

/**
 * 新增用户DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "新增用户请求参数")
public class UserAddDTO {

    @Schema(description = "邮箱地址", example = "test@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "用户昵称 (只能包含中文、字母、数字和下划线)", example = "技术博主", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 20, message = "昵称长度应在2-20个字符之间")
    @Pattern(
            regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]+$",
            message = "昵称只能包含中文、字母、数字和下划线"
    )
    private String nickname;

    /**
     * 密码校验
     */
    @Schema(description = "登录密码 (8-20位，必须包含字母和数字)", example = "Password123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度应在8-20位之间")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[\\x21-\\x7E]{8,20}$",
            message = "密码只能包含字母、数字和英文符号"
    )
    private String password;

    @Schema(description = "用户头像URL", example = "https://example.com/default-avatar.png")
    @URL(message = "头像URL格式不合法")
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    @Builder.Default
    private String avatar = Constants.DEFAULT_AVATAR;

    @Schema(description = "用户角色 (ADMIN:管理员, USER:普通用户)", example = "USER", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户角色不能为空")
    @Pattern(regexp = "^(ADMIN|USER)$", message = "角色必须是 ADMIN 或 USER")
    private String role;

    @Schema(description = "账号状态 (0:正常, 1:禁用, 2:锁定)", example = "0", defaultValue = "0")
    @Min(0)
    @Max(2)
    @Builder.Default
    private Integer status = BizStatus.User.NORMAL.getValue();

}