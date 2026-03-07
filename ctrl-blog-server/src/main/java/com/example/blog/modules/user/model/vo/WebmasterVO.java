package com.example.blog.modules.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "前台站长信息卡片VO")
public class WebmasterVO implements Serializable {

    // --- 1. 基本信息 ---
    @Schema(description = "站长ID", example = "1623456789012345678")
    private Long userId;

    @Schema(description = "昵称", example = "Ctrl管理员")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "个人简介/个性签名")
    private String bio;

    // --- 2. 博客统计信息 ---
    @Schema(description = "文章总数")
    private Long articleCount;

    @Schema(description = "分类总数")
    private Long categoryCount;

    @Schema(description = "标签总数")
    private Long tagCount;

    // --- 3. 社交链接 ---
    @Schema(description = "Github地址")
    private String github;
}