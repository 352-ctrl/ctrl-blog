package com.example.blog.modules.dashboard.service;

import com.example.blog.modules.dashboard.model.vo.DashboardVO;

/**
 * 仪表盘业务服务接口
 * 获取仪表盘相关的数据
 */
public interface DashboardService {

    /**
     * 获取仪表盘聚合数据
     * 包括：顶部卡片统计、近7天访问趋势、分类占比
     *
     * @return DashboardVO 聚合对象
     */
    DashboardVO getDashboardData();

}