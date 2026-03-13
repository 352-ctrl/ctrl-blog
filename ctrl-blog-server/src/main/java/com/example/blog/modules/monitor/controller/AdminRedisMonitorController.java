package com.example.blog.modules.monitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.common.base.Result;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.modules.monitor.model.dto.RedisKeyQueryDTO;
import com.example.blog.modules.monitor.model.vo.RedisMonitorVO;
import com.example.blog.modules.monitor.service.RedisMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/monitor/redis")
@AuthCheck(role = BizStatus.ROLE_SUPER_ADMIN)
@Tag(name = "后台/缓存监控")
public class AdminRedisMonitorController {

    @Resource
    private RedisMonitorService redisMonitorService;

    @GetMapping("/info")
    @Operation(summary = "获取Redis状态信息")
    public Result<RedisMonitorVO> getRedisInfo() {
        return Result.success(redisMonitorService.getRedisInfo());
    }

    @GetMapping("/keys")
    @Operation(summary = "分页获取键值列表")
    public Result<IPage<RedisMonitorVO.RedisKeyInfo>> getRedisKeys(RedisKeyQueryDTO dto) {
        return Result.success(redisMonitorService.getRedisKeys(dto));
    }
}