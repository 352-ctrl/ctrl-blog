package com.example.blog.modules.operation.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.blog.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 评论点赞实体类
 * 对应表：blog_comment_like
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("blog_comment_like")
public class CommentLike extends BaseEntity {

    /**
     * 评论ID
     */
    private Long commentId;

    /**
     * 用户ID
     */
    private Long userId;

}