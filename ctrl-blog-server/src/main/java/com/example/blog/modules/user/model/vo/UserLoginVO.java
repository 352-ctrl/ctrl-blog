package com.example.blog.modules.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登录成功响应对象 (VO)", title = "UserLoginVO")
public class UserLoginVO {
    @Schema(
            description = "认证令牌 (JWT Token)。推荐后续请求在 Header 中添加 `Authorization: Bearer <token>` 携带",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MTAwMSwi..."
    )
    private String token;

    @Schema(description = "当前登录用户的详细信息 (包含角色、昵称、头像等)")
    private UserVO userInfo;

    @Schema(
            description = "是否为账号恢复激活 (true-账号刚从注销冷静期恢复, false-正常登录)",
            example = "false"
    )
    private Boolean isRestored;
}