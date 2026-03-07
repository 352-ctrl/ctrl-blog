package com.example.blog.modules.operation.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.blog.common.base.BaseLogicEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 评论实体类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("blog_comment")
public class Comment extends BaseLogicEntity {

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数量
     */
    private Long likeCount;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 父评论ID，0表示顶级评论
     */
    private Long parentId;

    /**
     * 被回复的评论ID，0表示直接回复父评论
     */
    private Long replyCommentId;

    /**
     * 被回复的用户ID，用于显示和通知
     */
    private Long replyUserId;

}
