package com.example.blog.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.modules.system.model.entity.SysSensitiveWord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 敏感词数据访问层
 * 继承MyBatis-Plus基础Mapper，提供基本CRUD操作
 * 对应实体类：SysSensitiveWord
 * 对应数据库表：sys_sensitive_word
 *
 * @see SysSensitiveWord
 * @see BaseMapper
 */
@Mapper
public interface SysSensitiveWordMapper extends BaseMapper<SysSensitiveWord> {

}