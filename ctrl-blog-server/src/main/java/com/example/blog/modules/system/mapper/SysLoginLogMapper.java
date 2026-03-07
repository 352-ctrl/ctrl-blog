package com.example.blog.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.modules.system.model.entity.SysLoginLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface SysLoginLogMapper extends BaseMapper<SysLoginLog> {

    /**
     * 物理删除过期的日志
     */
    @Delete("DELETE FROM sys_login_log WHERE create_time < #{expireDate}")
    int physicalDeleteExpiredLogs(@Param("expireDate") LocalDateTime expireDate);

}