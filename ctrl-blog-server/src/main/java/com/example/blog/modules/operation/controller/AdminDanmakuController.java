package com.example.blog.modules.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.core.annotation.Log;
import com.example.blog.common.base.Result;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.operation.model.dto.DanmakuQueryDTO;
import com.example.blog.modules.operation.model.vo.DanmakuVO;
import com.example.blog.modules.operation.service.DanmakuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台弹幕管理控制器
 * 提供弹幕分页查询、单个及批量删除接口
 */
@RestController
@Validated
@RequestMapping("/api/v1/admin/danmakus")
@AuthCheck(role = BizStatus.ROLE_ADMIN)
@Tag(name = "后台/弹幕管理")
public class AdminDanmakuController {

    @Resource
    private DanmakuService danmakuService;

    /**
     * 分页查询弹幕列表
     */
    @GetMapping
    @Operation(summary = "分页查询弹幕列表", description = "后台管理页面的弹幕数据表格查询")
    public Result<IPage<DanmakuVO>> pageDanmakus(@Valid DanmakuQueryDTO queryDTO) {
        IPage<DanmakuVO> pageResult = danmakuService.pageAdminDanmakus(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 删除指定弹幕
     */
    @DeleteMapping("/{id}")
    @Log(module = "弹幕管理", type = "删除", desc = "管理员删除了指定弹幕")
    @Operation(summary = "删除弹幕", description = "根据ID逻辑删除单个弹幕。")
    public Result<Void> deleteDanmaku(@PathVariable @Positive(message = "弹幕ID非法") Long id) {
        danmakuService.deleteDanmakuById(id);
        return Result.success();
    }

    /**
     * 批量删除弹幕
     */
    @DeleteMapping("/batch")
    @Log(module = "弹幕管理", type = "批量删除", desc = "管理员批量移除了多个弹幕")
    @Operation(summary = "批量删除弹幕")
    public Result<Void> batchDeleteDanmakus(
            @RequestBody
            @NotEmpty(message = "请选择要删除的弹幕")
            List<
                    @NotNull(message = "ID不能为null")
                    @Positive(message = "ID必须为正数")
                            Long
                    > ids
    ) {
        danmakuService.batchDeleteDanmakus(ids);
        return Result.success();
    }
}