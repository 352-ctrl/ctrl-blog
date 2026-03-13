package com.example.blog.modules.monitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.modules.monitor.model.dto.RedisKeyQueryDTO;
import com.example.blog.modules.monitor.model.vo.RedisMonitorVO;

public interface RedisMonitorService {

    /**
     * 获取 Redis 基础运行信息
     * @return Redis监控实体
     */
    RedisMonitorVO getRedisInfo();

    /**
     * 模糊搜索并分页查询 Redis 键值列表
     * @param dto 查询条件
     * @return 分页数据
     */
    IPage<RedisMonitorVO.RedisKeyInfo> getRedisKeys(RedisKeyQueryDTO dto);
}