package com.example.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.entity.SysLog;
import com.example.blog.mapper.SysLogMapper;
import com.example.blog.service.SysLogService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 系统日志业务实现类
 * 实现保存系统日志的业务逻辑
 */
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {
    @Async // 开启异步记录，不影响业务响应速度
    @Override
    public void addLog(SysLog sysLog) {
        this.save(sysLog);
    }
}
