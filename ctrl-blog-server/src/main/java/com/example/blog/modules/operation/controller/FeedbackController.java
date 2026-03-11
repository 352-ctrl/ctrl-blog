package com.example.blog.modules.operation.controller;

import com.example.blog.core.annotation.RateLimit;
import com.example.blog.core.annotation.VerifyCaptcha;
import com.example.blog.common.base.Result;
import com.example.blog.modules.operation.model.dto.FeedbackAddDTO;
import com.example.blog.modules.operation.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 意见反馈控制器
 */
@RestController
@RequestMapping("/api/v1/feedbacks")
@Tag(name = "前台/意见反馈")
public class FeedbackController {

    @Resource
    private FeedbackService feedbackService;

    /**
     * 提交意见反馈 (允许游客)
     */
    @PostMapping
    @VerifyCaptcha
    @RateLimit(key = "ip", time = 60, count = 1)
    @Operation(summary = "提交意见反馈", description = "前台用户（含游客）提交建议、BUG等反馈")
    public Result<Void> addFeedback(@Valid @RequestBody FeedbackAddDTO addDTO) {
        feedbackService.addFeedback(addDTO);
        return Result.success();
    }

}