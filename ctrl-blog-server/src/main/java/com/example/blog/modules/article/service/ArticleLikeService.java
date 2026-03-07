package com.example.blog.modules.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.modules.article.model.entity.ArticleLike;

/**
 * 文章点赞业务服务接口
 * 纯粹的子实体服务，仅负责关联表和对应缓存的维护
 */
public interface ArticleLikeService extends IService<ArticleLike> {

    /**
     * 底层方法：保存点赞记录
     */
    void saveArticleLike(Long articleId, Long userId);

    /**
     * 底层方法：移除点赞记录
     */
    void removeArticleLike(Long articleId, Long userId);

    /**
     * 判断指定用户是否已点赞指定文章
     */
    boolean isLikedArticle(Long articleId, Long userId);

}