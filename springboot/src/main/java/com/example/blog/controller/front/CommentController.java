package com.example.blog.controller.front;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.annotation.AuthCheck;
import com.example.blog.annotation.Log;
import com.example.blog.annotation.RateLimit;
import com.example.blog.common.Result;
import com.example.blog.dto.comment.CommentAddDTO;
import com.example.blog.dto.comment.CommentQueryDTO;
import com.example.blog.service.CommentService;
import com.example.blog.vo.comment.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;

/**
 * 前台评论控制器
 * 提供评论发布、删除（仅限本人）及列表查询接口
 */
@RestController
@RequestMapping("/api/front/comments")
@Tag(name = "前台评论")
public class CommentController {

    @Resource
    private CommentService commentService;

    /**
     * 发布评论
     */
    @PostMapping
    @AuthCheck
    @RateLimit(key = "userId", time = 10, count = 1)
    @Log(module = "评论模块", type = "新增", desc = "用户发表了新评论/回复")
    @Operation(summary = "发布评论", description = "用户在文章下发布评论或回复他人。<br>后端会自动识别是“评论文章”还是“回复他人”。")
    public Result<Void> addComment(@Valid @RequestBody CommentAddDTO addDTO) {
        commentService.addComment(addDTO);
        return Result.success();
    }

    /**
     * 删除我的评论
     */
    @DeleteMapping("/{id}")
    @AuthCheck
    @Log(module = "评论模块", type = "删除", desc = "用户删除了自己的评论")
    @Operation(summary = "删除我的评论", description = "逻辑删除。**安全校验：** 接口内部会校验当前登录用户 ID 是否等于该评论的发布者 ID，防止越权删除。")
    public Result<Void> deleteMyComment(@PathVariable @Positive(message = "评论ID非法") Long id) {
        commentService.deleteCommentById(id);
        return Result.success();
    }

    /**
     * 分页查询评论列表
     */
    @GetMapping
    @Operation(summary = "分页查询评论", description = "查询指定文章下的评论列表。<br>返回数据结构通常为**树形结构**（一级评论下挂载子评论集合），按热度或时间排序。")
    public Result<IPage<CommentVO>> pageComments(@Valid CommentQueryDTO queryDTO) {
        IPage<CommentVO> pageResult = commentService.pageComments(queryDTO);
        return Result.success(pageResult);
    }
}