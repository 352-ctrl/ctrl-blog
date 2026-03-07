package com.example.blog.modules.user.model.dto;

import com.example.blog.common.enums.BizStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证用户DTO
 * 仅用于 Token 解析后的身份传递 (ThreadLocal/UserContext)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "JWT Token载荷/当前登录用户上下文信息")
public class UserPayloadDTO {

    @Schema(description = "用户ID", example = "1623456789012345678")
    private Long id;

    @Schema(description = "用户角色 (ADMIN/USER)", example = "ADMIN")
    private BizStatus.Role role;

    @Schema(description = "用户昵称", example = "技术博主")
    private String nickname;

}