package com.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.entity.ArticleLike;

/**
 * 文章点赞业务服务接口
 * 定义文章点赞相关的业务操作方法
 */
public interface ArticleLikeService extends IService<ArticleLike> {

    /**
     * 点赞文章
     *
     * @param articleId 文章ID
     */
    Long likeArticle(Long articleId);

    /**
     * 取消点赞文章
     *
     * @param articleId 文章ID
     */
    Long cancelLikeArticle(Long articleId);

    /**
     * 判断当前登录用户是否已点赞指定文章
     *
     * @param articleId 文章ID
     * @return true-已点赞，false-未点赞或未登录
     */
    boolean isLikedArticle(Long articleId);

}