package com.example.blog.modules.monitor.controller;

import com.example.blog.common.base.Result;
import com.example.blog.common.utils.IpUtils;
import com.example.blog.modules.monitor.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/visit")
@Tag(name = "前台-访问统计")
public class VisitController {

    @Resource
    private VisitService visitService;

    @PostMapping
    @Operation(summary = "主动上报站点访问", description = "前端路由切换时触发，包含 30 分钟 IP 防刷机制")
    public Result<Void> recordVisit(HttpServletRequest request) {
        // 获取真实 IP
        String ip = IpUtils.getIpAddr(request);

        visitService.recordVisit(ip);

        return Result.success();
    }
}