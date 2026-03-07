package com.example.blog.modules.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.modules.article.model.entity.ArticleFavorite;
import com.example.blog.core.exception.CustomerException;
import com.example.blog.modules.article.mapper.ArticleFavoriteMapper;
import com.example.blog.modules.article.service.ArticleFavoriteService;
import com.example.blog.common.utils.RedisUtil;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 文章收藏业务服务实现类
 */
@Service
public class ArticleFavoriteServiceImpl extends ServiceImpl<ArticleFavoriteMapper, ArticleFavorite> implements ArticleFavoriteService {

    @Resource
    private RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveArticleFavorite(Long articleId, Long userId) {
        ArticleFavorite favorite = ArticleFavorite.builder()
                .articleId(articleId)
                .userId(userId)
                .build();
        try {
            this.save(favorite);
        } catch (DuplicateKeyException e) {
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_ALREADY_FAVORITE);
        }

        String redisKey = RedisConstants.REDIS_ARTICLE_FAVORITE_KEY + articleId;
        redisUtil.sSet(redisKey, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeArticleFavorite(Long articleId, Long userId) {
        LambdaQueryWrapper<ArticleFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleFavorite::getArticleId, articleId)
                .eq(ArticleFavorite::getUserId, userId);

        boolean removed = this.remove(queryWrapper);
        if (!removed) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_LIKE_NOT_FOUND); // 这里复用了原有的文案
        }

        String redisKey = RedisConstants.REDIS_ARTICLE_FAVORITE_KEY + articleId;
        redisUtil.setRemove(redisKey, userId);
    }

    @Override
    public boolean isFavoriteArticle(Long articleId, Long userId) {
        Assert.notNull(articleId, "文章ID不能为空");
        if (userId == null) return false;

        ArticleFavorite articleFavorite = this.getOne(new LambdaQueryWrapper<ArticleFavorite>()
                .eq(ArticleFavorite::getArticleId, articleId)
                .eq(ArticleFavorite::getUserId, userId));
        return articleFavorite != null;
    }
}