package com.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.entity.ArticleFavorite;

/**
 * 文章收藏业务服务接口
 * 定义文章收藏相关的业务操作方法
 */
public interface ArticleFavoriteService extends IService<ArticleFavorite> {

    /**
     * 收藏文章
     *
     * @param articleId 文章ID
     */
    Long favoriteArticle(Long articleId);

    /**
     * 取消收藏文章
     *
     * @param articleId 文章ID
     */
    Long cancelFavoriteArticle(Long articleId);

    /**
     * 判断当前登录用户是否已收藏指定文章
     *
     * @param articleId 文章ID
     * @return true-已收藏，false-未收藏或未登录
     */
    boolean isFavoriteArticle(Long articleId);

}