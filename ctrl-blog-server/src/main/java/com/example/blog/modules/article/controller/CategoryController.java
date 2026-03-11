package com.example.blog.modules.article.controller;

import com.example.blog.common.base.Result;
import com.example.blog.modules.article.service.CategoryService;
import com.example.blog.modules.article.model.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台分类控制器
 * 提供分类列表查询等只读接口
 */
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "前台/分类")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    /**
     * 获取前台展示用的分类列表
     */
    @GetMapping
    @Operation(summary = "获取前台展示用的分类列表", description = "获取前台展示用的分类列表。通常用于首页侧边栏导航、文章分类筛选等场景。")
    public Result<List<CategoryVO>> listPortalCategories() {
        List<CategoryVO> categories = categoryService.listPortalCategories();
        return Result.success(categories);
    }

}