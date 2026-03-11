package com.example.blog.modules.operation.controller;

import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.core.annotation.RateLimit;
import com.example.blog.core.annotation.VerifyCaptcha;
import com.example.blog.common.base.Result;
import com.example.blog.modules.operation.model.dto.ReportAddDTO;
import com.example.blog.modules.operation.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内容举报控制器
 */
@RestController
@RequestMapping("/api/v1/reports")
@AuthCheck
@Tag(name = "前台/内容举报")
public class ReportController {

    @Resource
    private ReportService reportService;

    /**
     * 提交内容举报
     */
    @PostMapping
    @VerifyCaptcha
    @RateLimit(key = "ip", time = 60, count = 3)
    @Operation(summary = "提交内容举报", description = "前台用户登录后举报文章、评论或用户")
    public Result<Void> addReport(@Valid @RequestBody ReportAddDTO addDTO) {
        reportService.addReport(addDTO);
        return Result.success();
    }

}
