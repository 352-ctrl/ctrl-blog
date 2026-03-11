package com.example.blog.modules.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.core.annotation.Log;
import com.example.blog.common.base.Result;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.operation.model.dto.AdminCommentQueryDTO;
import com.example.blog.modules.operation.service.CommentService;
import com.example.blog.modules.operation.model.vo.AdminCommentVO;
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
 * 评论管理控制器
 * 提供评论的增删改查的RESTful API接口
 */
@RestController
@Validated
@RequestMapping("/api/v1/admin/comments")
@AuthCheck(role = BizStatus.ROLE_ADMIN)
@Tag(name = "后台/评论管理")
public class AdminCommentController {

    @Resource
    private CommentService commentService;

    /**
     * 删除指定评论
     */
    @DeleteMapping("/{id}")
    @Log(module = "评论管理", type = "删除", desc = "管理员删除了指定评论")
    @Operation(summary = "删除评论", description = "根据ID删除评论。**注意：** 如果该评论下有回复（子评论），通常会触发级联删除，将所有子评论一并移除。")
    public Result<Void> deleteComment(@PathVariable @Positive(message = "评论ID非法") Long id) {
        commentService.deleteCommentById(id);
        return Result.success();
    }

    /**
     * 批量删除评论
     */
    @DeleteMapping("/batch")
    @Log(module = "评论管理", type = "批量删除", desc = "管理员批量移除了多个评论")
    @Operation(summary = "批量删除评论")
    public Result<Void> batchDeleteComments(
            @RequestBody
            @NotEmpty(message = "请选择要删除的评论")
            List<
                    @NotNull(message = "ID不能为null")
                    @Positive(message = "ID必须为正数")
                            Long
                    > ids
    ) {
        commentService.batchDeleteComments(ids);
        return Result.success();
    }

    /**
     * 分页查询评论列表
     */
    @GetMapping
    @Operation(summary = "分页查询评论列表")
    public Result<IPage<AdminCommentVO>> pageComments(@Valid AdminCommentQueryDTO queryDTO) {
        IPage<AdminCommentVO> pageResult = commentService.pageAdminComments(queryDTO);
        return Result.success(pageResult);
    }

}