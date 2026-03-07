package com.example.blog.modules.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.modules.operation.model.entity.Notice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统公告数据访问层
 * 继承MyBatis-Plus基础Mapper，提供基本CRUD操作
 * 对应实体类：SysNotice
 * 对应数据库表：sys_notice
 *
 * @see Notice
 * @see BaseMapper
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {

}