package com.example.blog.modules.article.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.core.annotation.Log;
import com.example.blog.common.base.Result;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.article.model.dto.CategoryAddDTO;
import com.example.blog.modules.article.model.dto.CategoryQueryDTO;
import com.example.blog.modules.article.model.dto.CategoryUpdateDTO;
import com.example.blog.modules.article.service.CategoryService;
import com.example.blog.modules.article.model.vo.CategoryVO;
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
 * 文章分类管理控制器
 * 提供分类的增删改查的RESTful API接口
 */
@RestController
@Validated
@RequestMapping("/api/v1/admin/categories")
@AuthCheck(role = BizStatus.ROLE_ADMIN)
@Tag(name = "后台/分类管理")
public class AdminCategoryController {

    @Resource
    private CategoryService categoryService;

    /**
     * 新增分类
     */
    @PostMapping
    @Log(module = "分类管理", type = "新增", desc = "管理员创建了新分类")
    @Operation(summary = "新增分类")
    public Result<Void> addCategory(@Valid @RequestBody CategoryAddDTO addDTO) {
        categoryService.addCategory(addDTO);
        return Result.success();
    }

    /**
     * 更新分类
     */
    @PutMapping
    @Log(module = "分类管理", type = "修改", desc = "管理员更新了分类信息")
    @Operation(summary = "更新分类")
    public Result<Void> updateCategory(@Valid @RequestBody CategoryUpdateDTO updateDTO) {
        categoryService.updateCategory(updateDTO);
        return Result.success();
    }

    /**
     * 删除指定分类
     */
    @DeleteMapping("/{id}")
    @Log(module = "分类管理", type = "删除", desc = "管理员删除了指定分类")
    @Operation(summary = "删除指定分类", description = "删除前会校验该分类下是否有文章。若有关联文章则禁止删除。")
    public Result<Void> deleteCategory(@PathVariable @Positive(message = "分类ID非法") Long id) {
        categoryService.deleteCategoryById(id);
        return Result.success();
    }

    /**
     * 批量删除分类
     */
    @DeleteMapping("/batch")
    @Log(module = "分类管理", type = "批量删除", desc = "管理员批量移除了多个分类")
    @Operation(summary = "批量删除分类", description = "若选中分类中包含已关联文章的分类，可能导致部分或全部删除失败。")
    public Result<Void> batchDeleteCategories(
            @RequestBody
            @NotEmpty(message = "请选择要删除的分类")
            List<
                    @NotNull(message = "ID不能为null")
                    @Positive(message = "ID必须为正数")
                            Long
                    > ids
    ) {
        categoryService.batchDeleteCategories(ids);
        return Result.success();
    }

    /**
     * 获取单条分类详情（用于编辑回显）
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取分类详情(编辑用)")
    public Result<CategoryVO> getCategoryById(@PathVariable @Positive(message = "分类ID非法") Long id) {
        CategoryVO vo = categoryService.getCategoryById(id);
        return Result.success(vo);
    }

    /**
     * 分页查询分类列表
     */
    @GetMapping
    @Operation(summary = "分页查询分类列表")
    public Result<IPage<CategoryVO>> pageCategories(@Valid CategoryQueryDTO queryDTO) {
        IPage<CategoryVO> pageResult = categoryService.pageAdminCategories(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 获取分类列表
     */
    @GetMapping("/options")
    @Operation(summary = "获取所有分类", description = "不带分页，通常用于文章管理页的分类下拉列表。")
    public Result<List<CategoryVO>> listAllCategories() {
        List<CategoryVO> categories = categoryService.listAdminCategories();
        return Result.success(categories);
    }

}