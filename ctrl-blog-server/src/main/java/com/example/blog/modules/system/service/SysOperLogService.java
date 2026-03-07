package com.example.blog.modules.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.modules.system.model.dto.SysOperLogQueryDTO;
import com.example.blog.modules.system.model.entity.SysOperLog;
import com.example.blog.modules.system.model.vo.SysOperLogVO;

import java.time.LocalDateTime;

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

    /**
     * 清理过期的操作日志
     * @param logLimitDate 过期时间阈值
     * @return 删除的条数
     */
    int clearOperLogTrash(LocalDateTime logLimitDate);

}
