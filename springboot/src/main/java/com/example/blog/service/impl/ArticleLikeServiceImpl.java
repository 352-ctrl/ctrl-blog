package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.dto.user.UserPayloadDTO;
import com.example.blog.entity.Article;
import com.example.blog.entity.ArticleLike;
import com.example.blog.exception.CustomerException;
import com.example.blog.mapper.ArticleLikeMapper;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.service.ArticleLikeService;
import com.example.blog.utils.RedisUtil;
import com.example.blog.utils.UserContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 文章点赞业务服务实现类
 * 定义文章点赞相关的具体业务逻辑
 */
@Slf4j
@Service
public class ArticleLikeServiceImpl extends ServiceImpl<ArticleLikeMapper, ArticleLike> implements ArticleLikeService {

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 点赞文章
     *
     * @param articleId 文章ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long likeArticle(Long articleId) {
        // 1. 获取当前登录用户
        UserPayloadDTO user = UserContext.get();
        if (user == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }
        Long userId = user.getId();

        // 2. 插入数据库 (利用数据库唯一索引 uk_article_user 防止重复)
        ArticleLike like = ArticleLike.builder()
                .articleId(articleId)
                .userId(userId)
                .build();
        try {
            this.save(like);
        } catch (DuplicateKeyException e) {
            // 如果唯一索引冲突，说明已经点过赞了，抛出异常或直接返回
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_ALREADY_LIKED);
        }

        // 3. 同步写入 Redis Set
        // Key 格式: blog:article:like:{articleId}
        String redisKey = RedisConstants.REDIS_ARTICLE_LIKE_KEY + articleId;
        redisUtil.sSet(redisKey, userId);

        // 点赞
        articleMapper.incrLikeCount(articleId);

        String cacheKey = RedisConstants.REDIS_ARTICLE_DETAIL_PREFIX + articleId;
        redisUtil.delete(cacheKey);

        return Optional.ofNullable(articleMapper.selectOne(new LambdaQueryWrapper<Article>()
                        .select(Article::getLikeCount)
                        .eq(Article::getId, articleId)))
                .map(Article::getLikeCount)
                .orElse(0L);
    }

    /**
     * 取消点赞文章
     *
     * @param articleId 文章ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long cancelLikeArticle(Long articleId) {
        // 1. 获取当前登录用户
        UserPayloadDTO user = UserContext.get();
        if (user == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }
        Long userId = user.getId();

        // 2. 物理删除数据库记录
        LambdaQueryWrapper<ArticleLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleLike::getArticleId, articleId)
                .eq(ArticleLike::getUserId, userId);

        boolean removed = this.remove(queryWrapper);
        if (!removed) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_LIKE_NOT_FOUND);
        }

        // 3. 同步移除 Redis Set 中的记录
        String redisKey = RedisConstants.REDIS_ARTICLE_LIKE_KEY + articleId;
        redisUtil.setRemove(redisKey, userId);

        // 取消点赞
        articleMapper.decrLikeCount(articleId);

        String cacheKey = RedisConstants.REDIS_ARTICLE_DETAIL_PREFIX + articleId;
        redisUtil.delete(cacheKey);

        return Optional.ofNullable(articleMapper.selectOne(new LambdaQueryWrapper<Article>()
                        .select(Article::getLikeCount)
                        .eq(Article::getId, articleId)))
                .map(Article::getLikeCount)
                .orElse(0L);
    }

    /**
     * 判断当前登录用户是否已点赞指定文章
     *
     * @param articleId 文章ID
     */
    public boolean isLikedArticle(Long articleId) {
        // 获取当前登录用户
        UserPayloadDTO user = UserContext.get();
        if (user == null) {
            // 如果未登录，直接返回false
            return false;
        }
        ArticleLike articleLike = this.getOne(new LambdaQueryWrapper<ArticleLike>()
                .eq(ArticleLike::getArticleId, articleId)
                .eq(ArticleLike::getUserId, user.getId()));
        return articleLike != null;
    }

}