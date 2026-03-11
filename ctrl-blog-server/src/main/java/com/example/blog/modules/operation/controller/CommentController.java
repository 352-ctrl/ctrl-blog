package com.example.blog.modules.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.core.annotation.Log;
import com.example.blog.core.annotation.RateLimit;
import com.example.blog.common.base.Result;
import com.example.blog.modules.operation.model.dto.CommentAddDTO;
import com.example.blog.modules.operation.model.dto.CommentQueryDTO;
import com.example.blog.modules.operation.service.CommentService;
import com.example.blog.modules.operation.model.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 前台评论控制器
 * 提供评论发布、删除（仅限本人）及列表查询接口
 */
@Validated
@RestController
@RequestMapping("/api/v1/comments")
@Tag(name = "前台/评论")
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

    /**
     * 获取指定评论所在的分页页码 (用于消息中心跳转定位)
     */
    @GetMapping("/{id}/locator")
    @Operation(summary = "获取评论所在页码", description = "用于消息中心精准跳转时，计算评论在第几页")
    public Result<Integer> getCommentLocatorPage(@PathVariable Long id, @RequestParam(defaultValue = "10") Integer pageSize) {
        Integer pageNum = commentService.getCommentLocatorPage(id, pageSize);
        return Result.success(pageNum);
    }

    /**
     * 点赞评论
     */
    @PostMapping("/{commentId}/likes")
    @AuthCheck
    @RateLimit(key = "ip", time = 10, count = 5)
    @Operation(summary = "点赞评论", description = "登录用户对指定评论进行点赞。")
    public Result<Long> likeComment(@PathVariable @Positive(message = "评论ID非法") Long commentId) {
        Long likeCount = commentService.likeComment(commentId);
        return Result.success(likeCount);
    }

    /**
     * 取消点赞评论
     */
    @DeleteMapping("/{commentId}/likes")
    @AuthCheck
    @RateLimit(key = "ip", time = 10, count = 5)
    @Operation(summary = "取消点赞评论", description = "登录用户取消对指定评论的点赞。")
    public Result<Long> cancelLikeComment(@PathVariable @Positive(message = "评论ID非法") Long commentId) {
        Long likeCount = commentService.cancelLikeComment(commentId);
        return Result.success(likeCount);
    }
}