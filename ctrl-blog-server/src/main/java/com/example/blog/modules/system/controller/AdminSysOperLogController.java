package com.example.blog.modules.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.core.annotation.Log;
import com.example.blog.common.base.Result;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.system.model.dto.SysOperLogQueryDTO;
import com.example.blog.modules.system.service.SysOperLogService;
import com.example.blog.modules.system.model.vo.SysOperLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/admin/monitor/oper-log")
@AuthCheck(role = BizStatus.ROLE_SUPER_ADMIN)
@Tag(name = "后台/系统日志管理")
public class AdminSysOperLogController {

    @Resource
    private SysOperLogService sysOperLogService;

    @GetMapping
    @Operation(summary = "分页查询系统日志")
    public Result<IPage<SysOperLogVO>> pageQuery(SysOperLogQueryDTO queryDTO) {
        IPage<SysOperLogVO> page = sysOperLogService.pageAdminOperLog(queryDTO);
        return Result.success(page);
    }

    /**
     * 删除指定系统日志
     */
    @DeleteMapping("/{id}")
    @Log(module = "系统日志", type = "删除", desc = "管理员删除了指定系统日志")
    @Operation(summary = "删除系统日志")
    public Result<Void> deleteTag(@PathVariable @Positive(message = "系统日志ID非法") Long id) {
        sysOperLogService.removeById(id);
        return Result.success();
    }
    
    @DeleteMapping("/batch")
    @Log(module = "系统日志", type = "删除", desc = "管理员批量删除了系统日志")
    @Operation(summary = "批量删除系统日志")
    public Result<Void> deleteLogs(
            @RequestBody
            @NotEmpty(message = "请选择要删除的日志")
            List<
                    @NotNull(message = "ID不能为null")
                    @Positive(message = "ID必须为正数")
                            Long
                    > ids
    ) {
        sysOperLogService.removeByIds(ids);
        return Result.success();
    }
}