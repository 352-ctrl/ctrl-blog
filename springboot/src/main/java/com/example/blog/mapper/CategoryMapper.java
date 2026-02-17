package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.Category;
import org.apache.ibatis.annotations.Mapper;

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

}