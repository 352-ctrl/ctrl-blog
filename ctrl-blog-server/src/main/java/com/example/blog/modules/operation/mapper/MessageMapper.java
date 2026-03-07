package com.example.blog.modules.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.modules.operation.model.entity.Message;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 系统消息数据访问层
 * 处理系统消息、点赞通知、评论通知等数据的持久化操作
 * 对应实体类：Message
 * 对应数据库表：sys_message
 *
 * @see Message
 * @see BaseMapper
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 物理删除过期的系统消息
     */
    @Delete("DELETE FROM sys_message WHERE create_time <= #{limitDate}")
    int physicalDeleteExpired(@Param("limitDate") LocalDateTime limitDate);

}