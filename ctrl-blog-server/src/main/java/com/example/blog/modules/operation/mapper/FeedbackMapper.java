package com.example.blog.modules.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.modules.operation.model.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 意见反馈数据访问层
 * 继承MyBatis-Plus基础Mapper，提供基本CRUD操作
 * 对应实体类：Feedback
 * 对应数据库表：blog_feedback
 *
 * @see Feedback
 * @see BaseMapper
 */
@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {

}