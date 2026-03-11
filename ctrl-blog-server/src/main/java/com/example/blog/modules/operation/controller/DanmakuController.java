package com.example.blog.modules.operation.controller;

import com.example.blog.core.annotation.Log;
import com.example.blog.core.annotation.RateLimit;
import com.example.blog.common.base.Result;
import com.example.blog.modules.operation.model.dto.DanmakuAddDTO;
import com.example.blog.modules.operation.model.vo.DanmakuVO;
import com.example.blog.modules.operation.service.DanmakuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台弹幕控制器
 * 提供弹幕列表查询及发送功能
 */
@Validated
@RestController
@RequestMapping("/api/v1/danmakus")
@Tag(name = "前台/弹幕")
public class DanmakuController {

    @Resource
    private DanmakuService danmakuService;

    /**
     * 获取弹幕列表
     */
    @GetMapping
    @Operation(summary = "获取弹幕列表", description = "获取系统最新的100条有效弹幕，用于前台页面初始化显示。")
    public Result<List<DanmakuVO>> listDanmakus() {
        List<DanmakuVO> list = danmakuService.listDanmakus();
        return Result.success(list);
    }

    /**
     * 发送弹幕
     */
    @PostMapping
    @RateLimit(key = "ip", time = 10, count = 3)
    @Log(module = "弹幕模块", type = "新增", desc = "用户发送了新弹幕")
    @Operation(summary = "发送弹幕", description = "支持登录用户与匿名游客发送弹幕。")
    public Result<Void> addDanmaku(@Valid @RequestBody DanmakuAddDTO addDTO) {
        danmakuService.addDanmaku(addDTO);
        return Result.success();
    }
}