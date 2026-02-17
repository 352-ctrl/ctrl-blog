package com.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.entity.CommentLike;

import java.util.List;

/**
 * 评论点赞业务服务接口
 * 定义评论点赞相关的业务操作方法
 */
public interface CommentLikeService extends IService<CommentLike> {

    /**
     * 点赞评论
     *
     * @param commentId 评论ID
     */
    Long likeComment(Long commentId);

    /**
     * 取消点赞评论
     *
     * @param commentId 评论ID
     */
    Long cancelLikeComment(Long commentId);

    /**
     * 批量检查：在给定的评论ID列表中，哪些是当前用户已点赞的
     * @param commentIds 需要检查的评论ID列表
     * @return 用户已点赞的评论ID列表
     */
    List<Long> listLikedCommentIds(List<Long> commentIds);

}