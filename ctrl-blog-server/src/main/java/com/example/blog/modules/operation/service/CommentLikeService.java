package com.example.blog.modules.operation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.modules.operation.model.entity.CommentLike;

import java.util.List;

/**
 * 评论点赞业务服务接口
 * 纯粹的子实体服务，仅负责关联表和对应缓存的维护
 */
public interface CommentLikeService extends IService<CommentLike> {

    /**
     * 底层方法：保存点赞关联记录并同步缓存
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     */
    void saveCommentLike(Long commentId, Long userId);

    /**
     * 底层方法：移除点赞关联记录并同步缓存
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     */
    void removeCommentLike(Long commentId, Long userId);

    /**
     * 批量检查：在给定的评论ID列表中，哪些是指定用户已点赞的
     *
     * @param commentIds 需要检查的评论ID列表
     * @param userId     指定的查询用户ID
     * @return 该用户已点赞的评论ID列表
     */
    List<Long> listLikedCommentIds(List<Long> commentIds, Long userId);

}