package com.example.blog.modules.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.modules.article.model.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文章分类数据访问层
 * 继承MyBatis-Plus基础Mapper，提供基本CRUD操作
 * 对应实体类：Category
 * 对应数据库表：blog_category
 *
 * @see Category
 * @see BaseMapper
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 联表查询：获取所有包含已发布文章的分类 (用于前台分类展示)
     * 利用 JOIN 和 DISTINCT 直接在数据库层完成过滤和去重，性能极佳。
     */
    @Select("SELECT DISTINCT c.* FROM blog_category c " +
            "INNER JOIN blog_article a ON c.id = a.category_id " +
            "WHERE a.status = #{articleStatus} AND a.is_deleted = 0")
    List<Category> selectValidPortalCategories(@Param("articleStatus") Integer articleStatus);

}