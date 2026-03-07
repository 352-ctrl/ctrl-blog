package com.example.blog.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.modules.user.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户数据访问层
 * 继承MyBatis-Plus基础Mapper，提供基本CRUD操作
 * 对应实体类：User
 * 对应数据库表：sys_user
 *
 * @see User
 * @see BaseMapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}