package com.example.blog.modules.operation.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.modules.operation.model.entity.CommentLike;
import com.example.blog.core.exception.CustomerException;
import com.example.blog.modules.operation.mapper.CommentLikeMapper;
import com.example.blog.modules.operation.service.CommentLikeService;
import com.example.blog.common.utils.RedisUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论点赞业务服务实现类
 * 定义评论点赞相关的具体业务逻辑
 */
@Slf4j
@Service
public class CommentLikeServiceImpl extends ServiceImpl<CommentLikeMapper, CommentLike> implements CommentLikeService {

    @Resource
    private RedisUtil redisUtil;

    /**
     * 保存点赞记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCommentLike(Long commentId, Long userId) {
        CommentLike like = CommentLike.builder()
                .commentId(commentId)
                .userId(userId)
                .build();
        try {
            this.save(like);
        } catch (DuplicateKeyException e) {
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_ALREADY_LIKED);
        }

        // 同步写入 Redis Set
        String redisKey = RedisConstants.REDIS_COMMENT_LIKE_KEY + commentId;
        redisUtil.sSet(redisKey, userId);
    }

    /**
     * 移除点赞记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCommentLike(Long commentId, Long userId) {
        LambdaQueryWrapper<CommentLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, userId);

        boolean removed = this.remove(queryWrapper);
        if (!removed) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_LIKE_NOT_FOUND);
        }

        // 同步移除 Redis Set
        String redisKey = RedisConstants.REDIS_COMMENT_LIKE_KEY + commentId;
        redisUtil.setRemove(redisKey, userId);
    }

    @Override
    public List<Long> listLikedCommentIds(List<Long> commentIds, Long userId) {
        if (CollUtil.isEmpty(commentIds) || userId == null) {
            return Collections.emptyList();
        }

        return this.list(new LambdaQueryWrapper<CommentLike>()
                        .select(CommentLike::getCommentId)
                        .eq(CommentLike::getUserId, userId)
                        .in(CommentLike::getCommentId, commentIds))
                .stream()
                .map(CommentLike::getCommentId)
                .collect(Collectors.toList());
    }

}