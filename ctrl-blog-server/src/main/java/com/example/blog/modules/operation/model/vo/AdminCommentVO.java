package com.example.blog.modules.operation.model.vo;

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
@Schema(description = "后台评论管理列表对象 (VO)", title = "AdminCommentVO")
public class AdminCommentVO {
    @Schema(description = "评论ID", type = "string", example = "1623456789012345678")
    private Long id;

    @Schema(description = "评论内容", example = "这就去试试，博主牛逼！")
    private String content;

    @Schema(
            description = "发布时间",
            example = "2023-10-24 10:24:00",
            type = "string"
    )
    private LocalDateTime createTime;

    // --- 评论者信息 ---

    @Schema(description = "评论者ID", type = "string", example = "1623456789012345678")
    private Long userId;

    @Schema(description = "评论者昵称", example = "张三")
    private String userNickname;

    @Schema(description = "评论者头像", example = "https://example.com/avatar.jpg")
    private String userAvatar;

    // --- 来源文章信息 (后台列表必须字段) ---

    @Schema(description = "所属文章ID", type = "string", example = "1623456789012345678")
    private Long articleId;

    @Schema(description = "所属文章标题 (点击可跳转前台查看)", example = "Spring Boot 3.0 新特性全解析")
    private String articleTitle;

    // --- 回复/上下文信息 ---

    @Schema(
            description = "父评论ID (0或Null代表是顶层根评论，否则代表是子回复)",
            type = "string",
            example = "1623456789012345678"
    )
    private Long parentId;

    @Schema(description = "回复评论ID", type = "string", example = "1623456789012345678")
    private Long replyCommentId;

    @Schema(description = "被回复人ID", type = "string", example = "1623456789012345678")
    private Long replyUserId;

    @Schema(description = "被回复人昵称", example = "李四")
    private String replyUserNickname;

    @Schema(
            description = "被回复的内容摘要 (用于后台快速了解上下文，不用查表)",
            example = "确实，这个逻辑有点复杂..."
    )
    private String replyContent;
}