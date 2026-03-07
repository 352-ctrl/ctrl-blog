package com.example.blog.modules.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.modules.operation.model.entity.Report;
import org.apache.ibatis.annotations.Mapper;

/**
 * 内容举报投诉数据访问层
 * 继承MyBatis-Plus基础Mapper，提供基本CRUD操作
 * 对应实体类：Report
 * 对应数据库表：blog_report
 *
 * @see Report
 * @see BaseMapper
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {

}