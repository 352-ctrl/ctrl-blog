package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.dto.user.UserPayloadDTO;
import com.example.blog.entity.Article;
import com.example.blog.entity.ArticleFavorite;
import com.example.blog.exception.CustomerException;
import com.example.blog.mapper.ArticleFavoriteMapper;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.service.ArticleFavoriteService;
import com.example.blog.utils.RedisUtil;
import com.example.blog.utils.UserContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * 文章收藏业务服务实现类
 * 定义文章收藏相关的具体业务逻辑
 */
@Slf4j
@Service
public class ArticleFavoriteServiceImpl extends ServiceImpl<ArticleFavoriteMapper, ArticleFavorite> implements ArticleFavoriteService {

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 收藏文章
     *
     * @param articleId 文章ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long favoriteArticle(Long articleId) {
        Assert.notNull(articleId, "文章ID不能为空");

        // 1. 获取当前登录用户
        UserPayloadDTO user = UserContext.get();
        if (user == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }
        Long userId = user.getId();

        // 2. 插入数据库 (利用数据库唯一索引 uk_article_user 防止重复)
        ArticleFavorite favorite = ArticleFavorite.builder()
                .articleId(articleId)
                .userId(userId)
                .build();
        try {
            this.save(favorite);
        } catch (DuplicateKeyException e) {
            // 如果唯一索引冲突，说明已经点过赞了，抛出异常或直接返回
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_ALREADY_FAVORITE);
        }

        // 3. 同步写入 Redis Set
        // Key 格式: blog:article:like:{articleId}
        String redisKey = RedisConstants.REDIS_ARTICLE_FAVORITE_KEY + articleId;
        redisUtil.sSet(redisKey, userId);

        // 收藏
        articleMapper.incrFavoriteCount(articleId);

        String cacheKey = RedisConstants.REDIS_ARTICLE_DETAIL_PREFIX + articleId;
        redisUtil.delete(cacheKey);

        return Optional.ofNullable(articleMapper.selectOne(new LambdaQueryWrapper<Article>()
                        .select(Article::getFavoriteCount)
                        .eq(Article::getId, articleId)))
                .map(Article::getFavoriteCount)
                .orElse(0L);
    }

    /**
     * 取消收藏文章
     *
     * @param articleId 文章ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long cancelFavoriteArticle(Long articleId) {
        Assert.notNull(articleId, "文章ID不能为空");

        // 1. 获取当前登录用户
        UserPayloadDTO user = UserContext.get();
        if (user == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }
        Long userId = user.getId();

        // 2. 物理删除数据库记录
        LambdaQueryWrapper<ArticleFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleFavorite::getArticleId, articleId)
                .eq(ArticleFavorite::getUserId, userId);

        boolean removed = this.remove(queryWrapper);
        if (!removed) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_LIKE_NOT_FOUND);
        }

        // 3. 同步移除 Redis Set 中的记录
        String redisKey = RedisConstants.REDIS_ARTICLE_FAVORITE_KEY + articleId;
        redisUtil.setRemove(redisKey, userId);

        // 取消收藏
        articleMapper.decrFavoriteCount(articleId);

        String cacheKey = RedisConstants.REDIS_ARTICLE_DETAIL_PREFIX + articleId;
        redisUtil.delete(cacheKey);

        return Optional.ofNullable(articleMapper.selectOne(new LambdaQueryWrapper<Article>()
                        .select(Article::getFavoriteCount)
                        .eq(Article::getId, articleId)))
                .map(Article::getFavoriteCount)
                .orElse(0L);
    }

    /**
     * 判断当前登录用户是否已收藏指定文章
     *
     * @param articleId 文章ID
     */
    public boolean isFavoriteArticle(Long articleId) {
        Assert.notNull(articleId, "文章ID不能为空");

        // 获取当前登录用户
        UserPayloadDTO user = UserContext.get();
        if (user == null) {
            // 如果未登录，直接返回false
            return false;
        }
        ArticleFavorite articleFavorite = this.getOne(new LambdaQueryWrapper<ArticleFavorite>()
                .eq(ArticleFavorite::getArticleId, articleId)
                .eq(ArticleFavorite::getUserId, user.getId()));
        return articleFavorite != null;
    }


}