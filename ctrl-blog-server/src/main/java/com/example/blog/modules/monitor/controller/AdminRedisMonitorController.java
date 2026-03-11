package com.example.blog.modules.monitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.common.base.Result;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.monitor.model.dto.RedisKeyQueryDTO;
import com.example.blog.modules.monitor.model.vo.RedisMonitorVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/monitor/redis")
@AuthCheck(role = BizStatus.ROLE_SUPER_ADMIN)
@Tag(name = "后台/缓存监控")
public class AdminRedisMonitorController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/info")
    @Operation(summary = "获取Redis状态信息", description = "获取Redis版本、内存、流量等基础信息")
    public Result<RedisMonitorVO> getRedisInfo() {
        RedisConnectionFactory connectionFactory = stringRedisTemplate.getConnectionFactory();
        if (connectionFactory == null) {
            return Result.error("Redis 连接失败");
        }

        RedisConnection connection = connectionFactory.getConnection();
        Properties info = connection.serverCommands().info();

        if (info == null) {
            return Result.error("获取 Redis INFO 失败");
        }

        String maxMemory = info.getProperty("maxmemory_human");
        String memoryConfig = (maxMemory == null || "0B".equals(maxMemory)) ? "不限制" : maxMemory;

        RedisMonitorVO vo = RedisMonitorVO.builder()
                .version(info.getProperty("redis_version"))
                .runMode("standalone".equals(info.getProperty("redis_mode")) ? "单机" : "集群")
                .port(info.getProperty("tcp_port"))
                .uptime(info.getProperty("uptime_in_days"))
                .clientCount(info.getProperty("connected_clients"))
                .memoryConfig(memoryConfig)
                .aofEnabled("0".equals(info.getProperty("aof_enabled")) ? "否" : "是")
                .rdbStatus("ok".equals(info.getProperty("rdb_last_bgsave_status")) ? "成功" : "失败")
                .keyCount(connection.serverCommands().dbSize())
                .networkInput(info.getProperty("instantaneous_input_kbps"))
                .networkOutput(info.getProperty("instantaneous_output_kbps"))
                .build();

        return Result.success(vo);
    }

    @GetMapping("/keys")
    @Operation(summary = "分页获取键值列表", description = "支持按键名模糊搜索及分页展示")
    public Result<IPage<RedisMonitorVO.RedisKeyInfo>> getRedisKeys(RedisKeyQueryDTO dto) {
        // 1. 拼接模糊查询匹配符
        String pattern = StringUtils.hasText(dto.getKeyword()) ? "*" + dto.getKeyword() + "*" : "*";

        // 2. 获取所有匹配的 Keys (注意: 生产环境如果Key上千万建议改为 SCAN 指令)
        Set<String> keySet = stringRedisTemplate.keys(pattern);
        List<String> allKeys = new ArrayList<>(keySet != null ? keySet : Collections.emptySet());

        // 排序保证分页顺序稳定
        Collections.sort(allKeys);

        // 3. 内存分页计算
        int total = allKeys.size();
        int start = (dto.getPageNum() - 1) * dto.getPageSize();
        int end = Math.min(start + dto.getPageSize(), total);

        List<RedisMonitorVO.RedisKeyInfo> pageList = new ArrayList<>();

        // 4. 截取当前页的数据并组装详细信息
        if (start < total) {
            List<String> subKeys = allKeys.subList(start, end);
            for (String key : subKeys) {
                String type = stringRedisTemplate.type(key).code();
                Long expire = stringRedisTemplate.getExpire(key);

                String ttlDisplay = "未知";
                if (expire != null) {
                    if (expire == -1) ttlDisplay = "永不过期";
                    else if (expire == -2) ttlDisplay = "已过期/不存在";
                    else ttlDisplay = expire + " s";
                }

                long size = 0L;
                try {
                    size = switch (type) {
                        case "string" -> stringRedisTemplate.opsForValue().size(key);
                        case "list" -> stringRedisTemplate.opsForList().size(key);
                        case "set" -> stringRedisTemplate.opsForSet().size(key);
                        case "zset" -> stringRedisTemplate.opsForZSet().size(key);
                        case "hash" -> stringRedisTemplate.opsForHash().size(key);
                        default -> 0L;
                    };
                } catch (Exception ignored) {}

                pageList.add(RedisMonitorVO.RedisKeyInfo.builder()
                        .key(key)
                        .type(type)
                        .ttl(ttlDisplay)
                        .size(size)
                        .build());
            }
        }

        // 5. 封装为 IPage 格式返回
        IPage<RedisMonitorVO.RedisKeyInfo> page = new Page<>(dto.getPageNum(), dto.getPageSize(), total);
        page.setRecords(pageList);

        return Result.success(page);
    }
}