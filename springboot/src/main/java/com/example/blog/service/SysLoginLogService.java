package com.example.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.dto.SysLoginLogQueryDTO;
import com.example.blog.entity.SysLoginLog;
import com.example.blog.vo.SysLoginLogVO;

import java.time.LocalDateTime;

public interface SysLoginLogService extends IService<SysLoginLog> {

    /**
     * 异步记录登录日志
     */
    void recordLoginLog(String username, Integer status, String message, String ip, String userAgent);

    /**
     * 分页查询登录日志
     */
    IPage<SysLoginLogVO> pageAdminLoginLog(SysLoginLogQueryDTO queryDTO);

    /**
     * 清理过期的登录日志
     * @param logLimitDate 过期时间阈值
     * @return 删除的条数
     */
    int clearLoginLogTrash(LocalDateTime logLimitDate);

}
