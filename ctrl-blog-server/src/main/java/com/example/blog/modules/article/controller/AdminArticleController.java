package com.example.blog.modules.article.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.core.annotation.Log;
import com.example.blog.common.base.Result;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.article.model.dto.ArticleAddDTO;
import com.example.blog.modules.article.model.dto.ArticleGenerateSummaryDTO;
import com.example.blog.modules.article.model.dto.ArticleQueryDTO;
import com.example.blog.modules.article.model.dto.ArticleUpdateDTO;
import com.example.blog.modules.article.service.AiSummaryService;
import com.example.blog.modules.article.service.ArticleService;
import com.example.blog.modules.article.model.vo.AdminArticleVO;
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
 * 文章后台管理控制器
 * 提供文章的增删改查等RESTful API接口
 */
@RestController
@Validated
@RequestMapping("/api/v1/admin/articles")
@AuthCheck(role = BizStatus.ROLE_ADMIN)
@Tag(name = "后台/文章管理")
public class AdminArticleController {

    @Resource
    private ArticleService articleService;

    @Resource
    private AiSummaryService aiSummaryService;

    /**
     * 新增文章
     */
    @PostMapping
    @Log(module = "文章管理", type = "新增", desc = "管理员发布了新文章")
    @Operation(summary = "新增文章")
    public Result<Void> addArticle(@Valid @RequestBody ArticleAddDTO addDTO) {
        articleService.addArticle(addDTO);
        return Result.success();
    }

    /**
     * 更新文章
     */
    @PutMapping
    @Log(module = "文章管理", type = "修改", desc = "管理员修改了文章内容或状态")
    @Operation(summary = "更新文章")
    public Result<Void> updateArticle(@Valid @RequestBody ArticleUpdateDTO updateDTO) {
        articleService.updateArticle(updateDTO);
        return Result.success();
    }

    /**
     * 删除指定文章
     */
    @DeleteMapping("/{id}")
    @Log(module = "文章管理", type = "删除", desc = "管理员删除了指定文章")
    @Operation(summary = "删除指定文章")
    public Result<Void> deleteArticle(@PathVariable @Positive(message = "文章ID非法") Long id) {
        articleService.deleteArticleById(id);
        return Result.success();
    }

    /**
     * 批量删除文章
     */
    @DeleteMapping("/batch")
    @Log(module = "文章管理", type = "批量删除", desc = "管理员执行了批量删除文章操作")
    @Operation(summary = "批量删除文章")
    public Result<Void> batchDeleteArticles(
            @RequestBody
            @NotEmpty(message = "请选择要删除的文章")
            List<
                    @NotNull(message = "ID不能为null")
                    @Positive(message = "ID必须为正数")
                            Long
                    > ids
    ) {
        articleService.batchDeleteArticles(ids);
        return Result.success();
    }

    /**
     * 获取单条文章详情（用于编辑回显）
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取文章详情(编辑用)")
    public Result<AdminArticleVO> getArticle(@PathVariable @Positive(message = "文章ID非法") Long id) {
        AdminArticleVO vo = articleService.getArticleForEdit(id);
        return Result.success(vo);
    }

    /**
     * 分页查询文章列表
     */
    @GetMapping
    @Operation(summary = "分页查询文章列表")
    public Result<IPage<AdminArticleVO>> pageArticles(@Valid ArticleQueryDTO queryDTO) {
        IPage<AdminArticleVO> pageResult = articleService.pageAdminArticles(queryDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/summary-generation")
    @Operation(summary = "AI生成摘要")
    public Result<String> generateSummary(@RequestBody @Validated ArticleGenerateSummaryDTO dto) {
        String summary = aiSummaryService.generateSummary(dto.getTitle(), dto.getContent());
        return Result.success(summary);
    }
}