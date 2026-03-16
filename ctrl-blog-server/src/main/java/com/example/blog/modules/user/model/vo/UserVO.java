package com.example.blog.modules.user.model.vo;

import com.example.blog.core.annotation.CheckSensitive;
import com.example.blog.common.jackson.EmailDesensitizeSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户展示层对象 (VO)", title = "UserVO")
public class UserVO {
    @Schema(description = "用户ID", type = "string", example = "1623456789012345678")
    private Long id;

    @JsonSerialize(using = EmailDesensitizeSerializer.class)
    @Schema(description = "用户邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "个人简介", example = "热爱技术，热爱生活")
    @Size(max = 200, message = "个人简介不能超过200个字符")
    @CheckSensitive(message = "个人简介包含违规词汇，请修改")
    private String bio;

    @Schema(description = "用户头像URL", example = "https://example.com/default-avatar.png")
    private String avatar;

    @Schema(
            description = "用户角色 (ADMIN-管理员, USER-普通用户)",
            example = "USER"
    )
    private String role;

    @Schema(
            description = "账号状态 (0-正常, 1-禁用, 2-注销冷静期, 3-已注销)",
            example = "0"
    )
    private Integer status;

    @Schema(
            description = "注册时间",
            example = "2023-10-24 10:24:00",
            type = "string"
    )
    private LocalDateTime createTime;

    @Schema(
            description = "封禁解封时间",
            example = "2023-10-31 10:24:00",
            type = "string"
    )
    private LocalDateTime disableEndTime;

    @Schema(description = "封禁原因", example = "发布违规不良信息")
    private String disableReason;
}
