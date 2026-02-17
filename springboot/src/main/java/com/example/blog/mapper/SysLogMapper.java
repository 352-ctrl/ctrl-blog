package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.SysLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface SysLogMapper extends BaseMapper<SysLog> {

    /**
     * 物理删除过期的日志
     */
    @Delete("DELETE FROM sys_log WHERE create_time < #{expireDate}")
    int deleteExpiredLogs(@Param("expireDate") LocalDateTime expireDate);

}