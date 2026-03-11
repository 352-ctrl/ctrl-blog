package com.example.blog.modules.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.core.annotation.Log;
import com.example.blog.common.base.Result;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.operation.model.dto.NoticeAddDTO;
import com.example.blog.modules.operation.model.dto.NoticeQueryDTO;
import com.example.blog.modules.operation.model.dto.NoticeUpdateDTO;
import com.example.blog.modules.operation.service.NoticeService;
import com.example.blog.modules.operation.model.vo.AdminNoticeVO;
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
 * 公告管理控制器
 * 提供公告的增删改查 RESTful API 接口
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/notices")
@AuthCheck(role = BizStatus.ROLE_ADMIN)
@Tag(name = "后台/公告管理")
public class AdminNoticeController {

    @Resource
    private NoticeService noticeService;

    /**
     * 新增公告
     */
    @PostMapping
    @Log(module = "公告管理", type = "新增", desc = "管理员创建了新公告")
    @Operation(summary = "新增公告")
    public Result<Void> addNotice(@Valid @RequestBody NoticeAddDTO addDTO) {
        noticeService.addNotice(addDTO);
        return Result.success();
    }

    /**
     * 更新公告
     */
    @PutMapping
    @Log(module = "公告管理", type = "修改", desc = "管理员更新了公告信息")
    @Operation(summary = "更新公告")
    public Result<Void> updateNotice(@Valid @RequestBody NoticeUpdateDTO updateDTO) {
        noticeService.updateNotice(updateDTO);
        return Result.success();
    }

    /**
     * 删除指定公告
     */
    @DeleteMapping("/{id}")
    @Log(module = "公告管理", type = "删除", desc = "管理员删除了指定公告")
    @Operation(summary = "删除公告")
    public Result<Void> deleteNotice(@PathVariable @Positive(message = "公告ID非法") Long id) {
        noticeService.deleteNoticeById(id);
        return Result.success();
    }

    /**
     * 批量删除公告
     */
    @DeleteMapping("/batch")
    @Log(module = "公告管理", type = "批量删除", desc = "管理员批量移除了多个公告")
    @Operation(summary = "批量删除公告")
    public Result<Void> batchDeleteNotices(
            @RequestBody
            @NotEmpty(message = "请选择要删除的公告")
            List<
                    @NotNull(message = "ID不能为null")
                    @Positive(message = "ID必须为正数")
                            Long
                    > ids
    ) {
        noticeService.batchDeleteNotices(ids);
        return Result.success();
    }

    /**
     * 获取单条公告详情（用于编辑回显）
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取公告详情(编辑用)")
    public Result<AdminNoticeVO> getNoticeById(@PathVariable @Positive(message = "公告ID非法") Long id) {
        AdminNoticeVO vo = noticeService.getNoticeById(id);
        return Result.success(vo);
    }

    /**
     * 分页查询公告列表
     */
    @GetMapping
    @Operation(summary = "分页查询公告列表")
    public Result<IPage<AdminNoticeVO>> pageNotices(@Valid NoticeQueryDTO queryDTO) {
        IPage<AdminNoticeVO> pageResult = noticeService.pageAdminNotices(queryDTO);
        return Result.success(pageResult);
    }
}