package com.example.blog.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.annotation.AuthCheck;
import com.example.blog.annotation.Log;
import com.example.blog.common.Result;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.dto.SysLoginLogQueryDTO;
import com.example.blog.service.SysLoginLogService;
import com.example.blog.vo.SysLoginLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/monitor/login-log")
@AuthCheck(role = BizStatus.ROLE_ADMIN)
@Tag(name = "后台登录日志管理")
public class SysLoginLogController {

    @Resource
    private SysLoginLogService sysLoginLogService;

    @GetMapping
    @Operation(summary = "分页查询登录日志")
    public Result<IPage<SysLoginLogVO>> pageQuery(SysLoginLogQueryDTO queryDTO) {
        IPage<SysLoginLogVO> page = sysLoginLogService.pageAdminLoginLog(queryDTO);
        return Result.success(page);
    }

    /**
     * 删除指定登录日志
     */
    @DeleteMapping("/{id}")
    @Log(module = "登录日志", type = "删除", desc = "管理员删除了指定登录日志")
    @Operation(summary = "删除登录日志")
    public Result<Void> deleteTag(@PathVariable @Positive(message = "登录日志ID非法") Long id) {
        sysLoginLogService.removeById(id);
        return Result.success();
    }
    
    @DeleteMapping("/batch")
    @Log(module = "登录日志", type = "删除", desc = "管理员批量删除了登录日志")
    @Operation(summary = "批量删除登录日志")
    public Result<Void> deleteLogs(
            @RequestBody
            @NotEmpty(message = "请选择要删除的日志")
            List<
                    @NotNull(message = "ID不能为null")
                    @Positive(message = "ID必须为正数")
                            Long
                    > ids
    ) {
        sysLoginLogService.removeByIds(ids);
        return Result.success();
    }
}