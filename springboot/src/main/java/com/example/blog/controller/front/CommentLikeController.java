package com.example.blog.controller.front;

import com.example.blog.annotation.RateLimit;
import com.example.blog.common.Result;
import com.example.blog.service.CommentLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台评论点赞控制器
 * 处理用户对评论的点赞与取消点赞操作
 */
@RestController
@RequestMapping("/api/front/comments")
@Tag(name = "前台评论点赞")
public class CommentLikeController {

    @Resource
    private CommentLikeService commentLikeService;

    /**
     * 点赞评论
     * POST /api/front/comments/{commentId}/like
     */
    @PostMapping("/{commentId}/like")
    @RateLimit(key = "ip", time = 10, count = 5)
    @Operation(summary = "点赞评论", description = "用户对指定评论进行点赞。<br>如果用户未登录，需要在过滤器或切面中拦截并返回未授权错误。")
    public Result<Long> likeComment(@PathVariable @Positive(message = "评论ID非法") Long commentId) {
        Long likeCount = commentLikeService.likeComment(commentId);
        return Result.success(likeCount);
    }

    /**
     * 取消点赞评论
     * POST /api/front/comments/{commentId}/cancel-like
     */
    @PostMapping("/{commentId}/cancel-like")
    @RateLimit(key = "ip", time = 10, count = 5)
    @Operation(summary = "取消点赞评论", description = "用户取消对指定评论的点赞。")
    public Result<Long> cancelLikeComment(@PathVariable @Positive(message = "评论ID非法") Long commentId) {
        Long likeCount = commentLikeService.cancelLikeComment(commentId);
        return Result.success(likeCount);
    }
}