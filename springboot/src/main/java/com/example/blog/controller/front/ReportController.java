package com.example.blog.controller.front;

import com.example.blog.annotation.AuthCheck;
import com.example.blog.annotation.RateLimit;
import com.example.blog.annotation.VerifyCaptcha;
import com.example.blog.common.Result;
import com.example.blog.dto.report.ReportAddDTO;
import com.example.blog.service.ReportService;
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
@RequestMapping("/api/front/report")
@AuthCheck
@Tag(name = "内容举报管理")
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
