package com.example.blog.service;

import com.example.blog.entity.SysLog;

/**
 * 系统日志业务服务接口
 * 定义保存系统日志的业务操作方法
 */
public interface SysLogService {

    /**
     * 保存系统日志
     *
     * @param sysLog 系统日志实体类
     */
    void addLog(SysLog sysLog);
}
