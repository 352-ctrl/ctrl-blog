package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

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

}