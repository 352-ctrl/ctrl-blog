package com.example.blog.modules.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.modules.article.model.entity.ArticleFavorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章收藏数据访问层
 * 处理用户对文章的收藏操作（收藏/取消收藏）
 * 对应实体类：ArticleFavorite
 * 对应数据库表："blog_article_favorite"
 *
 * @see ArticleFavorite
 * @see BaseMapper
 */
@Mapper
public interface ArticleFavoriteMapper extends BaseMapper<ArticleFavorite> {

}