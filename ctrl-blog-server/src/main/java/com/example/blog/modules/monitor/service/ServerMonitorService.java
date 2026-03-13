package com.example.blog.modules.monitor.service;

import com.example.blog.modules.monitor.model.vo.ServerMonitorVO;

public interface ServerMonitorService {

    /**
     * 获取服务器硬件及 JVM 环境监控信息
     * @return 监控数据实体
     */
    ServerMonitorVO getServerInfo();
}