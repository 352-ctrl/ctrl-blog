package com.example.blog.modules.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.modules.article.model.entity.ArticleLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章点赞数据访问层
 * 处理用户对文章的点赞操作（点赞/取消点赞）
 * 对应实体类：ArticleLike
 * 对应数据库表：blog_article_like
 *
 * @see ArticleLike
 * @see BaseMapper
 */
@Mapper
public interface ArticleLikeMapper extends BaseMapper<ArticleLike> {

}