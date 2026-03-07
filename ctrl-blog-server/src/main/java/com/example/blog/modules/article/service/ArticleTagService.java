package com.example.blog.modules.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.modules.article.model.entity.ArticleTag;

import java.util.List;

/**
 * 文章标签业务服务接口
 * 定义标签相关的业务操作方法
 */
public interface ArticleTagService extends IService<ArticleTag> {

    /**
     * 根据标签ID列表，找出拥有这些所有标签的文章ID
     * @param tagIds 标签ID集合
     * @return 文章ID列表
     */
    List<Long> listArticleIdsByTagIds(List<Long> tagIds);

    /**
     * 更新文章的标签关联关系
     * (会自动计算增量，执行添加或删除操作)
     *
     * @param articleId 文章ID
     * @param tagIds    新的标签ID列表
     */
    void updateArticleTags(Long articleId, List<Long> tagIds);

}