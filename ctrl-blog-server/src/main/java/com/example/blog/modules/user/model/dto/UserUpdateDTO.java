package com.example.blog.modules.user.model.dto;

import com.example.blog.core.annotation.CheckSensitive;
import com.example.blog.common.enums.BizStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

/**
 * 修改用户DTO (后台管理专用)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "后台修改用户请求参数")
public class UserUpdateDTO {

    @Schema(description = "用户ID", example = "1623456789012345678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    @Positive(message = "用户ID必须为正整数")
    private Long id;

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

    @Schema(description = "用户头像URL", example = "https://example.com/avatar.jpg")
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatar;

    @Schema(description = "用户角色", example = "USER", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户角色不能为空")
    private BizStatus.Role role;

    @Schema(description = "账号状态", example = "NORMAL")
    private BizStatus.User status;


    @Schema(description = "封禁到期时间", example = "2023-12-31 23:59:59")
    private LocalDateTime disableEndTime;

    @Schema(description = "封禁原因", example = "发布违规不良信息")
    @Size(max = 255, message = "封禁原因不能超过255个字符")
    @CheckSensitive(message = "封禁原因包含违规词汇，请修改")
    private String disableReason;

}