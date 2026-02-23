package com.example.blog.controller.front;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.common.Result;
import com.example.blog.common.constants.Constants;
import com.example.blog.dto.article.ArticleQueryDTO;
import com.example.blog.dto.article.ArticleVisitorDTO;
import com.example.blog.service.ArticleService;
import com.example.blog.utils.IpUtils;
import com.example.blog.vo.article.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章前台控制器
 * 提供文章的查询、归档、搜索等 RESTful API 接口
 */
@Validated
@RestController
@RequestMapping("/api/front/articles")
@Tag(name = "前台文章")
public class ArticleController {

    @Resource
    private ArticleService articleService;

    /**
     * 获取文章详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取文章详情", description = "根据文章ID查询完整内容、作者信息及评论数。")
    public Result<ArticleDetailVO> getArticleDetail(@PathVariable @Positive(message = "文章ID非法") Long id) {
        ArticleDetailVO detailVO = articleService.getArticleDetail(id);
        return Result.success(detailVO);
    }

    /**
     * 分页查询文章列表 (前台列表)
     */
    @GetMapping
    @Operation(summary = "分页查询文章列表", description = "支持通过标题搜索、分类筛选、标签筛选。若无筛选条件，则返回默认时间流列表（已包含置顶逻辑）。")
    public Result<IPage<ArticleCardVO>> pageArticles(@Valid ArticleQueryDTO queryDTO) {
        IPage<ArticleCardVO> pageResult = articleService.pageArticles(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 获取文章归档
     */
    @GetMapping("/archive")
    @Operation(summary = "获取文章归档", description = "按年份/月份分组返回文章简要信息，用于归档页或时间轴展示。")
    public Result<List<ArchiveAggregateVO>> listArchives() {
        List<ArchiveAggregateVO> archives = articleService.listArchives();
        return Result.success(archives);
    }

    /**
     * 获取全站搜索索引
     */
    @GetMapping("/search/index")
    @Operation(summary = "获取搜索索引", description = "返回所有已发布文章的轻量级摘要（ID、标题、摘要），用于前端实现本地模糊搜索。")
    public Result<List<ArticleSearchVO>> listSearchIndexes() {
        List<ArticleSearchVO> searchIndexes = articleService.listSearchIndexes();
        return Result.success(searchIndexes);
    }

    /**
     * 获取首页轮播图列表
     */
    @GetMapping("/carousel")
    @Operation(summary = "获取首页轮播图", description = "返回被标记为“轮播”的已发布文章列表，按时间倒序排列（用于首页顶部展示）。")
    public Result<List<ArticleCarouselVO>> listCarousel() {
        List<ArticleCarouselVO> carouselList = articleService.listCarousel();
        return Result.success(carouselList);
    }

    /**
     * 获取侧边栏热门文章
     */
    @GetMapping("/hot")
    @Operation(summary = "获取热门文章", description = "按浏览量倒序返回前5篇热门文章，专用于侧边栏展示。")
    public Result<List<ArticleHotVO>> listHotArticles() {
        List<ArticleHotVO> hotArticles = articleService.listHotArticles();
        return Result.success(hotArticles);
    }

    /**
     * 增加阅读量接口
     * 前端在文章加载完成后，单独调用此接口
     * POST /article/{id}/view
     */
    @PostMapping("/{id}/view")
    @Operation(summary = "增加阅读量", description = "防刷机制：同一IP+UA在60秒内只算一次")
    public Result<Void> incrementView(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        // 1. 提取 IP
        String ip = IpUtils.getIpAddr(request);

        // 2. 提取 UA
        String userAgent = request.getHeader(Constants.HEADER_USER_AGENT);
        // 防御性编程：防止数据库报错
        if (userAgent != null && userAgent.length() > 500) {
            userAgent = userAgent.substring(0, 500);
        }

        // 3. 组装 DTO
        ArticleVisitorDTO visitorDTO = ArticleVisitorDTO.builder()
                .articleId(id)
                .ip(ip)
                .userAgent(userAgent)
                .build();

        // 4. 指挥 Service 干活
        articleService.incrementViewCount(visitorDTO);

        return Result.success();
    }
}