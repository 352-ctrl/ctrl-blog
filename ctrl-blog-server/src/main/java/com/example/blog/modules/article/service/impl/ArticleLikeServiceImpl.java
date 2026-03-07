package com.example.blog.modules.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.modules.article.model.entity.ArticleLike;
import com.example.blog.core.exception.CustomerException;
import com.example.blog.modules.article.mapper.ArticleLikeMapper;
import com.example.blog.modules.article.service.ArticleLikeService;
import com.example.blog.common.utils.RedisUtil;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 文章点赞业务服务实现类
 */
@Service
public class ArticleLikeServiceImpl extends ServiceImpl<ArticleLikeMapper, ArticleLike> implements ArticleLikeService {

    @Resource
    private RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveArticleLike(Long articleId, Long userId) {
        ArticleLike like = ArticleLike.builder()
                .articleId(articleId)
                .userId(userId)
                .build();
        try {
            this.save(like);
        } catch (DuplicateKeyException e) {
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_ALREADY_LIKED);
        }

        String redisKey = RedisConstants.REDIS_ARTICLE_LIKE_KEY + articleId;
        redisUtil.sSet(redisKey, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeArticleLike(Long articleId, Long userId) {
        LambdaQueryWrapper<ArticleLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleLike::getArticleId, articleId)
                .eq(ArticleLike::getUserId, userId);

        boolean removed = this.remove(queryWrapper);
        if (!removed) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_LIKE_NOT_FOUND);
        }

        String redisKey = RedisConstants.REDIS_ARTICLE_LIKE_KEY + articleId;
        redisUtil.setRemove(redisKey, userId);
    }

    @Override
    public boolean isLikedArticle(Long articleId, Long userId) {
        Assert.notNull(articleId, "文章ID不能为空");
        if (userId == null) return false;

        ArticleLike articleLike = this.getOne(new LambdaQueryWrapper<ArticleLike>()
                .eq(ArticleLike::getArticleId, articleId)
                .eq(ArticleLike::getUserId, userId));
        return articleLike != null;
    }
}