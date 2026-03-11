package com.example.blog.modules.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.core.annotation.Log;
import com.example.blog.common.base.Result;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.system.model.dto.ChangeStatusDto;
import com.example.blog.modules.system.model.dto.SysJobAddDTO;
import com.example.blog.modules.system.model.dto.SysJobQueryDTO;
import com.example.blog.modules.system.model.dto.SysJobUpdateDTO;
import com.example.blog.modules.system.service.SysJobService;
import com.example.blog.modules.system.model.vo.SysJobVO;
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
 * 定时任务调度控制器
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/jobs")
@AuthCheck(role = BizStatus.ROLE_SUPER_ADMIN)
@Tag(name = "后台/定时任务管理")
public class AdminJobController {

    @Resource
    private SysJobService sysJobService;

    /**
     * 分页查询定时任务列表
     */
    @GetMapping
    @Operation(summary = "分页查询任务列表")
    public Result<IPage<SysJobVO>> pageJobs(SysJobQueryDTO sysJobQueryDTO) {
        IPage<SysJobVO> sysJobVOIPage = sysJobService.pageAdminJobs(sysJobQueryDTO);
        return Result.success(sysJobVOIPage);
    }

    /**
     * 获取详细信息
     */
    @GetMapping(value = "/{id}")
    public Result<SysJobVO> getJobById(@PathVariable @Positive(message = "任务ID非法") Long id) {
        return Result.success(sysJobService.getJobById(id));
    }

    /**
     * 新增定时任务
     */
    @PostMapping
    @Log(module = "任务管理", type = "新增", desc = "管理员创建了新的任务")
    @Operation(summary = "新增任务")
    public Result<Void> addJob(@RequestBody SysJobAddDTO addDTO) {
        sysJobService.addJob(addDTO);
        return Result.success();
    }

    /**
     * 修改定时任务
     */
    @PutMapping
    @Log(module = "任务管理", type = "修改", desc = "管理员更新了任务信息")
    @Operation(summary = "修改任务", description = "修改任务的 Cron 表达式或执行逻辑")
    public Result<Void> updateJob(@RequestBody SysJobUpdateDTO updateDTO) {
        sysJobService.updateJob(updateDTO);
        return Result.success();
    }

    /**
     * 定时任务状态修改 (暂停/恢复)
     */
    @PostMapping("/{id}/status")
    @Log(module = "任务管理", type = "修改状态", desc = "管理员修改了任务运行状态")
    @Operation(summary = "修改任务状态", description = "暂停或恢复任务")
    public Result<Void> changeStatus(
            @PathVariable @Positive(message = "任务ID非法") Long id,
            @RequestBody @Valid ChangeStatusDto statusDto
    ) {
        sysJobService.changeStatus(id, statusDto.getStatus());
        return Result.success();
    }

    /**
     * 定时任务立即执行一次
     */
    @PostMapping("/{id}/run")
    @Log(module = "任务管理", type = "执行", desc = "管理员手动触发了一次任务")
    @Operation(summary = "立即执行一次", description = "不影响原有计划，单独触发一次任务")
    public Result<Void> run(@PathVariable @Positive(message = "任务ID非法") Long id) {
        sysJobService.run(id);
        return Result.success();
    }

    /**
     * 删除指定任务
     */
    @DeleteMapping("/{id}")
    @Log(module = "任务管理", type = "删除", desc = "管理员删除了指定任务")
    @Operation(summary = "删除任务")
    public Result<Void> deleteJob(@PathVariable @Positive(message = "标签ID非法") Long id) {
        sysJobService.deleteJobById(id);
        return Result.success();
    }

    /**
     * 批量删除定时任务
     */
    @DeleteMapping("/batch")
    @Log(module = "任务管理", type = "批量删除", desc = "管理员批量移除了多个任务")
    @Operation(summary = "批量删除任务")
    public Result<Void> batchDeleteJobs(
            @RequestBody
            @NotEmpty(message = "请选择要删除的定时任务")
            List<
                    @NotNull(message = "ID不能为null")
                    @Positive(message = "ID必须为正数")
                            Long
                    > ids
    ) {
        sysJobService.batchDeleteJobs(ids);
        return Result.success();
    }

}
