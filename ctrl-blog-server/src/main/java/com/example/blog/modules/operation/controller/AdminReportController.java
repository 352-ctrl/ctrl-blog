package com.example.blog.modules.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.core.annotation.Log;
import com.example.blog.common.base.Result;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.operation.model.dto.ReportProcessDTO;
import com.example.blog.modules.operation.model.dto.ReportQueryDTO;
import com.example.blog.modules.operation.service.ReportService;
import com.example.blog.modules.operation.model.vo.AdminReportVO;
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
 * 后台内容举报控制器
 */
@RestController
@RequestMapping("/api/v1/admin/reports")
@AuthCheck(role = BizStatus.ROLE_ADMIN)
@Tag(name = "后台/内容举报管理")
public class AdminReportController {

    @Resource
    private ReportService reportService;

    /**
     * 分页查询举报列表
     */
    @GetMapping
    @Operation(summary = "分页查询举报列表", description = "管理员分页查询所有举报记录")
    public Result<IPage<AdminReportVO>> pageAdminReports(@Valid ReportQueryDTO queryDTO) {
        IPage<AdminReportVO> page = reportService.pageAdminReports(queryDTO);
        return Result.success(page);
    }

    /**
     * 处理举报
     */
    @PostMapping("/process")
    @Operation(summary = "处理举报", description = "管理员审核处理举报，判断是否属实并联动惩罚")
    public Result<Void> processReport(@Valid @RequestBody ReportProcessDTO processDTO) {
        reportService.processReport(processDTO);
        return Result.success();
    }

    /**
     * 删除单条举报记录
     */
    @DeleteMapping("/{id}")
    @Log(module = "内容举报", type = "删除", desc = "管理员删除了单条举报记录")
    @Operation(summary = "删除单条举报记录", description = "管理员删除指定的举报记录")
    public Result<Void> deleteReportById(@PathVariable @Positive(message = "举报ID非法") Long id) {
        reportService.deleteReportById(id);
        return Result.success();
    }

    /**
     * 批量删除举报记录
     */
    @DeleteMapping("/batch")
    @Log(module = "内容举报", type = "批量删除", desc = "管理员执行了批量删除举报操作")
    @Operation(summary = "批量删除举报记录", description = "管理员批量删除多个举报记录")
    public Result<Void> batchDeleteReports(
            @RequestBody
            @NotEmpty(message = "请选择要删除的举报")
            List<
                    @NotNull(message = "ID不能为null")
                    @Positive(message = "ID必须为正数")
                            Long
                    > ids
    ) {
        reportService.batchDeleteReports(ids);
        return Result.success();
    }

}
