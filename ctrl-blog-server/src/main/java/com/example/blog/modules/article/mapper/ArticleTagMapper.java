package com.example.blog.modules.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.modules.article.model.entity.ArticleTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章标签关联数据访问层
 * 处理文章与标签的多对多关联关系
 * 对应实体类：ArticleTag
 * 对应数据库表：blog_article_tag
 *
 * @see ArticleTag
 * @see BaseMapper
 */
@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {

}