package com.example.blog.modules.dashboard.controller;

import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.common.base.Result;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.dashboard.service.DashboardService;
import com.example.blog.modules.dashboard.model.vo.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台仪表盘控制器
 * 提供首页统计卡片、图表所需的数据接口
 */
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@AuthCheck(role = BizStatus.ROLE_ADMIN)
@Tag(name = "后台/仪表盘管理", description = "提供管理员首页的核心统计数据接口")
public class AdminDashboardController {

    @Resource
    private DashboardService dashboardService;

    @GetMapping
    @Operation(
            summary = "获取仪表盘聚合数据",
            description = "聚合接口：一次性返回顶部统计卡片（文章/用户/评论/访问量）、流量趋势折线图数据以及文章分类占比数据。"
    )
    public Result<DashboardVO> getDashboardData() {
        DashboardVO dashboardVO = dashboardService.getDashboardData();
        return Result.success(dashboardVO);
    }

}