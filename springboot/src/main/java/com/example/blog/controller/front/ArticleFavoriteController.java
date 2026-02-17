package com.example.blog.controller.front;

import com.example.blog.annotation.RateLimit;
import com.example.blog.common.Result;
import com.example.blog.service.ArticleFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台文章收藏控制器
 * 处理用户对文章的收藏与取消收藏操作
 */
@RestController
@RequestMapping("/api/front/articles")
@Tag(name = "前台文章收藏")
public class ArticleFavoriteController {

    @Resource
    private ArticleFavoriteService articleFavoriteService;

    /**
     * 收藏文章
     * POST /api/front/articles/{articleId}/favorite
     */
    @PostMapping("/{articleId}/favorite")
    @RateLimit(key = "ip", time = 10, count = 5)
    @Operation(summary = "收藏文章", description = "用户对指定文章进行收藏。<br>如果用户未登录，需要在过滤器或切面中拦截并返回未授权错误。")
    public Result<Long> favoriteArticle(@PathVariable @Positive(message = "文章ID非法") Long articleId) {
        Long favoriteCount = articleFavoriteService.favoriteArticle(articleId);
        return Result.success(favoriteCount);
    }

    /**
     * 取消收藏文章
     * POST /api/front/articles/{articleId}/cancel-favorite
     */
    @PostMapping("/{articleId}/cancel-favorite")
    @RateLimit(key = "ip", time = 10, count = 5)
    @Operation(summary = "取消收藏文章", description = "用户取消对指定文章的收藏。")
    public Result<Long> cancelFavoriteArticle(@PathVariable @Positive(message = "文章ID非法") Long articleId) {
        Long favoriteCount = articleFavoriteService.cancelFavoriteArticle(articleId);
        return Result.success(favoriteCount);
    }
}