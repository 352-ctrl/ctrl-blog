package com.example.blog.modules.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.modules.article.model.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文章标签数据访问层
 * 继承MyBatis-Plus基础Mapper，提供基本CRUD操作
 * 对应实体类：Tag
 * 对应数据库表：blog_tag
 *
 * @see Tag
 * @see BaseMapper
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 联表查询：获取所有有已发布文章关联的标签 (用于前台标签云)
     * 利用 JOIN 和 DISTINCT 直接在数据库层完成过滤和去重，性能极佳。
     */
    @Select("SELECT DISTINCT t.* FROM blog_tag t " +
            "INNER JOIN blog_article_tag at ON t.id = at.tag_id " +
            "INNER JOIN blog_article a ON at.article_id = a.id " +
            "WHERE a.status = #{articleStatus} AND a.is_deleted = 0")
    List<Tag> selectValidPortalTags(@Param("articleStatus") Integer articleStatus);

    /**
     * 联表查询：根据文章ID获取关联的标签
     */
    @Select("SELECT t.* FROM blog_tag t " +
            "INNER JOIN blog_article_tag at ON t.id = at.tag_id " +
            "WHERE at.article_id = #{articleId}")
    List<Tag> selectTagsByArticleId(@Param("articleId") Long articleId);

}