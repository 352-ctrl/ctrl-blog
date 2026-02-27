package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.SysMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统消息数据访问层
 * 处理系统消息、点赞通知、评论通知等数据的持久化操作
 * 对应实体类：SysMessage
 * 对应数据库表：sys_message
 *
 * @see SysMessage
 * @see BaseMapper
 */
@Mapper
public interface SysMessageMapper extends BaseMapper<SysMessage> {

}