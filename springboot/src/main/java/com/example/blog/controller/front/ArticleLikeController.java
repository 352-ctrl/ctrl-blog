package com.example.blog.controller.front;

import com.example.blog.annotation.RateLimit;
import com.example.blog.common.Result;
import com.example.blog.service.ArticleLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台文章点赞控制器
 * 处理用户对文章的点赞与取消点赞操作
 */
@RestController
@RequestMapping("/api/front/articles")
@Tag(name = "前台文章点赞")
public class ArticleLikeController {

    @Resource
    private ArticleLikeService articleLikeService;

    /**
     * 点赞文章
     * POST /api/front/articles/{articleId}/like
     */
    @PostMapping("/{articleId}/like")
    @RateLimit(key = "ip", time = 10, count = 5)
    @Operation(summary = "点赞文章", description = "用户对指定文章进行点赞。<br>如果用户未登录，需要在过滤器或切面中拦截并返回未授权错误。")
    public Result<Long> likeArticle(@PathVariable @Positive(message = "文章ID非法") Long articleId) {
        Long likeCount = articleLikeService.likeArticle(articleId);
        return Result.success(likeCount);
    }

    /**
     * 取消点赞文章
     * POST /api/front/articles/{articleId}/cancel-like
     */
    @PostMapping("/{articleId}/cancel-like")
    @RateLimit(key = "ip", time = 10, count = 5)
    @Operation(summary = "取消点赞文章", description = "用户取消对指定文章的点赞。")
    public Result<Long> cancelLikeArticle(@PathVariable @Positive(message = "文章ID非法") Long articleId) {
        Long likeCount = articleLikeService.cancelLikeArticle(articleId);
        return Result.success(likeCount);
    }
}