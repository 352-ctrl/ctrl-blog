package com.example.blog.modules.monitor.controller;

import com.example.blog.common.base.Result;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.modules.monitor.model.vo.ServerMonitorVO;
import com.example.blog.modules.monitor.service.ServerMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/monitor/server")
@AuthCheck(role = BizStatus.ROLE_SUPER_ADMIN)
@Tag(name = "后台/服务监控")
public class AdminServerMonitorController {

    @Resource
    private ServerMonitorService serverMonitorService;

    @GetMapping("/info")
    @Operation(summary = "获取服务器信息")
    public Result<ServerMonitorVO> getServerInfo() {
        return Result.success(serverMonitorService.getServerInfo());
    }
}