package com.example.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.dto.SysOperLogQueryDTO;
import com.example.blog.entity.SysOperLog;
import com.example.blog.vo.SysOperLogVO;

/**
 * 系统日志业务服务接口
 * 定义保存系统日志的业务操作方法
 */
public interface SysOperLogService extends IService<SysOperLog> {

    /**
     * 保存系统日志
     *
     * @param sysOperLog 系统日志实体类
     */
    void addLog(SysOperLog sysOperLog);

    /**
     * 分页查询系统日志
     */
    IPage<SysOperLogVO> pageAdminOperLog(SysOperLogQueryDTO queryDTO);

}
