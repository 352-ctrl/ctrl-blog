package com.example.blog.modules.user.model.dto;

import com.example.blog.core.annotation.CheckSensitive;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

/**
 * 用户信息更新DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户个人信息更新请求参数")
public class UserProfileUpdateDTO {

    @Schema(description = "用户昵称 (只能包含中文、字母、数字和下划线)", example = "极客博主", requiredMode = Schema.RequiredMode.REQUIRED)
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

    @Schema(description = "头像URL", example = "https://example.com/new-avatar.jpg")
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatar;

}