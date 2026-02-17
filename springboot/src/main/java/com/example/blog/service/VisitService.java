package com.example.blog.service;

import java.util.Map;

public interface VisitService {
    /**
     * 记录一次页面访问
     */
    void recordVisit();

    /**
     * 获取网站历史总访问量 (PV)
     * 对应 Dashboard 的数字卡片
     *
     * @return 总访问量
     */
    Long countTotalVisits();

    /**
     * 获取近7天流量趋势
     * 对应 Dashboard 的折线图
     *
     * @return 包含 "dates" 和 "counts" 的 Map
     */
    Map<String, Object> getVisitTrendStats();

    /**
     * 同步 Redis 中的今日访问量到数据库
     * 该方法由 Quartz 定时任务调用
     */
    void syncVisitDataToDb();
}