package com.example.blog.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "用户ID", example = "1623456789012345678")
    private Long id;

    @Schema(description = "用户邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "用户头像URL", example = "https://example.com/default-avatar.png")
    private String avatar;

    @Schema(
            description = "用户角色 (ADMIN-管理员, USER-普通用户)",
            example = "USER",
            allowableValues = {"ADMIN", "USER"}
    )
    private String role;

    @Schema(
            description = "账号状态 (0-正常, 1-禁用, 2-注销冷静期, 3-已注销)",
            example = "0",
            allowableValues = {"0", "1", "2", "3"}
    )
    private Integer status;

    @Schema(
            description = "注册时间",
            example = "2023-10-24 10:24:00",
            type = "string",
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
