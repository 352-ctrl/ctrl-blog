package com.example.blog.modules.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.core.annotation.Log;
import com.example.blog.common.base.Result;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.operation.model.dto.FeedbackProcessDTO;
import com.example.blog.modules.operation.model.dto.FeedbackQueryDTO;
import com.example.blog.modules.operation.service.FeedbackService;
import com.example.blog.modules.operation.model.vo.AdminFeedbackVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台意见反馈控制器
 */
@RestController
@RequestMapping("/api/v1/admin/feedbacks")
@AuthCheck(role = BizStatus.ROLE_ADMIN)
@Tag(name = "后台/意见反馈管理")
public class AdminFeedbackController {

    @Resource
    private FeedbackService feedbackService;

    /**
     * 分页查询反馈列表
     */
    @GetMapping
    @Operation(summary = "分页查询反馈列表", description = "管理员分页查询所有用户反馈")
    public Result<IPage<AdminFeedbackVO>> pageAdminFeedbacks(@Valid FeedbackQueryDTO queryDTO) {
        IPage<AdminFeedbackVO> page = feedbackService.pageAdminFeedbacks(queryDTO);
        return Result.success(page);
    }

    /**
     * 处理反馈
     */
    @PostMapping("/process")
    @Operation(summary = "处理反馈", description = "管理员回复反馈并更改状态")
    public Result<Void> processFeedback(@Valid @RequestBody FeedbackProcessDTO processDTO) {
        feedbackService.processFeedback(processDTO);
        return Result.success();
    }

    /**
     * 删除单条反馈记录
     */
    @DeleteMapping("/{id}")
    @Log(module = "意见反馈", type = "删除", desc = "管理员删除了单条反馈记录")
    @Operation(summary = "删除单条反馈记录", description = "管理员删除指定的反馈记录")
    public Result<Void> deleteFeedbackById(@PathVariable @Positive(message = "反馈ID非法") Long id) {
        feedbackService.deleteFeedbackById(id);
        return Result.success();
    }

    /**
     * 批量删除反馈记录
     */
    @DeleteMapping("/batch")
    @Log(module = "意见反馈", type = "批量删除", desc = "管理员执行了批量删除反馈操作")
    @Operation(summary = "批量删除反馈记录", description = "管理员批量删除多个反馈记录")
    public Result<Void> batchDeleteFeedbacks(
            @RequestBody
            @NotEmpty(message = "请选择要删除的反馈")
            List<
                    @NotNull(message = "ID不能为null")
                    @Positive(message = "ID必须为正数")
                            Long
                    > ids
    ) {
        feedbackService.batchDeleteFeedbacks(ids);
        return Result.success();
    }

}