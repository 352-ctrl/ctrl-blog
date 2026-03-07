package com.example.blog.modules.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.modules.article.model.entity.ArticleFavorite;

/**
 * 文章收藏业务服务接口
 * 纯粹的子实体服务，仅负责关联表和对应缓存的维护
 */
public interface ArticleFavoriteService extends IService<ArticleFavorite> {

    /**
     * 底层方法：保存收藏记录
     */
    void saveArticleFavorite(Long articleId, Long userId);

    /**
     * 底层方法：移除收藏记录
     */
    void removeArticleFavorite(Long articleId, Long userId);

    /**
     * 判断指定用户是否已收藏指定文章
     */
    boolean isFavoriteArticle(Long articleId, Long userId);

}