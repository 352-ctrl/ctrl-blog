package com.example.blog.modules.operation.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "前台评论展示对象 (VO)", title = "CommentVO")
public class CommentVO {
    @Schema(description = "评论ID", type = "string", example = "1623456789012345678")
    private Long id;

    @Schema(description = "评论内容", example = "博主写得太好了，受益匪浅！")
    private String content;

    @Schema(description = "点赞数量", type = "string", example = "100")
    private Long likeCount;

    @Schema(description = "当前登录用户是否已点赞", example = "true")
    @Builder.Default
    private boolean liked = false;

    @Schema(
            description = "发布时间",
            example = "2023-10-24 10:24:00",
            type = "string"
    )
    private LocalDateTime createTime;

    // --- 评论者信息 ---

    @Schema(description = "评论者ID", type = "string", example = "1623456789012345678")
    private Long userId;

    @Schema(description = "评论者昵称", example = "热心网友")
    private String userNickname;

    @Schema(description = "评论者头像", example = "https://example.com/avatar/1.jpg")
    private String userAvatar;

    // --- 回复/层级信息 ---

    @Schema(
            description = "父评论ID (0或Null代表是顶层根评论，否则代表是子回复)",
            type = "string",
            example = "1623456789012345678"
    )
    private Long parentId;

    @Schema(description = "被回复的评论ID (用于定位具体回复了哪一条)", type = "string", example = "1623456789012345678")
    private Long replyCommentId;

    @Schema(description = "被回复人的ID", type = "string", example = "1623456789012345678")
    private Long replyUserId;

    @Schema(description = "被回复人的昵称 (也就是 @某某某)", example = "技术大佬")
    private String replyUserNickname;

    // --- 子评论列表 ---

    @Schema(description = "子评论/回复数量", example = "5")
    private Integer replyCount;

    @Schema(description = "子评论/回复列表 (树形结构)")
    @Builder.Default
    private List<CommentVO> children = new ArrayList<>();
}