package com.example.blog.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.dto.user.UserPayloadDTO;
import com.example.blog.entity.Comment;
import com.example.blog.entity.CommentLike;
import com.example.blog.exception.CustomerException;
import com.example.blog.mapper.CommentLikeMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.service.CommentLikeService;
import com.example.blog.utils.RedisUtil;
import com.example.blog.utils.UserContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 评论点赞业务服务实现类
 * 定义评论点赞相关的具体业务逻辑
 */
@Slf4j
@Service
public class CommentLikeServiceImpl extends ServiceImpl<CommentLikeMapper, CommentLike> implements CommentLikeService {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 点赞评论
     *
     * @param commentId 评论ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long likeComment(Long commentId) {
        Assert.notNull(commentId, "评论ID不能为空");

        // 1. 获取当前登录用户
        UserPayloadDTO user = UserContext.get();
        if (user == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }
        Long userId = user.getId();

        // 2. 插入数据库 (利用数据库唯一索引 uk_comment_user 防止重复)
        CommentLike like = CommentLike.builder()
                .commentId(commentId)
                .userId(userId)
                .build();
        try {
            this.save(like);
        } catch (DuplicateKeyException e) {
            // 如果唯一索引冲突，说明已经点过赞了，抛出异常或直接返回
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_ALREADY_LIKED);
        }

        // 3. 同步写入 Redis Set
        // Key 格式: blog:comment:like:{commentId}
        String redisKey = RedisConstants.REDIS_COMMENT_LIKE_KEY + commentId;
        redisUtil.sSet(redisKey, userId);

        // 点赞
        commentMapper.incrLikeCount(commentId);

        return Optional.ofNullable(commentMapper.selectOne(new LambdaQueryWrapper<Comment>()
                        .select(Comment::getLikeCount)
                        .eq(Comment::getId, commentId)))
                .map(Comment::getLikeCount)
                .orElse(0L);
    }

    /**
     * 取消点赞评论
     *
     * @param commentId 评论ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long cancelLikeComment(Long commentId) {
        Assert.notNull(commentId, "评论ID不能为空");

        // 1. 获取当前登录用户
        UserPayloadDTO user = UserContext.get();
        if (user == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }
        Long userId = user.getId();

        // 2. 物理删除数据库记录
        LambdaQueryWrapper<CommentLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, userId);

        boolean removed = this.remove(queryWrapper);
        if (!removed) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_LIKE_NOT_FOUND);
        }

        // 3. 同步移除 Redis Set 中的记录
        String redisKey = RedisConstants.REDIS_COMMENT_LIKE_KEY + commentId;
        redisUtil.setRemove(redisKey, userId);

        // 取消点赞
        commentMapper.decrLikeCount(commentId);

        return Optional.ofNullable(commentMapper.selectOne(new LambdaQueryWrapper<Comment>()
                        .select(Comment::getLikeCount)
                        .eq(Comment::getId, commentId)))
                .map(Comment::getLikeCount)
                .orElse(0L);
    }

    /**
     * 根据评论 ID 列表检查当前登录用户的点赞状态
     *
     * @param commentIds 评论 ID 列表
     * @return 当前用户已点赞的评论 ID 列表
     */
    @Override
    public List<Long> listLikedCommentIds(List<Long> commentIds) {
        if (CollUtil.isEmpty(commentIds)) {
            return Collections.emptyList();
        }

        // 1. 获取当前登录用户
        UserPayloadDTO user = UserContext.get();
        if (user == null) {
            // 如果未登录，直接返回空列表
            return Collections.emptyList();
        }
        Long userId = user.getId();

        // 2. 批量查询已点赞的评论 ID
        return this.list(new LambdaQueryWrapper<CommentLike>()
                        .select(CommentLike::getCommentId)
                        .eq(CommentLike::getUserId, userId)
                        .in(CommentLike::getCommentId, commentIds))
                .stream()
                .map(CommentLike::getCommentId)
                .collect(Collectors.toList());
    }

}