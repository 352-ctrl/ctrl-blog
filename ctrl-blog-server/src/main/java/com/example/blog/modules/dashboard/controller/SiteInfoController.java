package com.example.blog.modules.dashboard.controller;

import com.example.blog.common.base.Result;
import com.example.blog.modules.dashboard.service.SiteInfoService;
import com.example.blog.modules.user.model.vo.WebmasterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/site")
@Tag(name = "前台/站点信息模块")
public class SiteInfoController {

    @Resource
    private SiteInfoService siteInfoService;

    @Operation(summary = "获取站长卡片信息")
    @GetMapping("/webmaster")
    public Result<WebmasterVO> getWebmasterInfo() {
        return Result.success(siteInfoService.getWebmasterInfo());
    }
}