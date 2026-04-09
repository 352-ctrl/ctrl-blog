package com.example.blog.modules.user.model.dto;

import com.example.blog.core.annotation.CheckSensitive;
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
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
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
    @CheckSensitive(message = "用户昵称包含违规词汇，请修改")
    private String nickname;

    @Schema(description = "个人简介", example = "热爱技术，热爱生活")
    @Size(max = 200, message = "个人简介不能超过200个字符")
    @CheckSensitive(message = "个人简介包含违规词汇，请修改")
    private String bio;

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
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatar;

    @Schema(description = "用户角色", example = "USER", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户角色不能为空")
    private BizStatus.Role role;

    @Schema(description = "账号状态", example = "NORMAL", defaultValue = "NORMAL")
    @NotNull(message = "账号状态不能为空")
    @Builder.Default
    private BizStatus.User status = BizStatus.User.NORMAL;

}