package com.example.blog.modules.article.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.core.annotation.Log;
import com.example.blog.common.base.Result;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.article.model.dto.TagAddDTO;
import com.example.blog.modules.article.model.dto.TagQueryDTO;
import com.example.blog.modules.article.model.dto.TagUpdateDTO;
import com.example.blog.modules.article.service.TagService;
import com.example.blog.modules.article.model.vo.TagVO;
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
 * 文章标签管理控制器
 * 提供标签的增删改查的 RESTful API 接口
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/tags")
@AuthCheck(role = BizStatus.ROLE_ADMIN)
@Tag(name = "后台/标签管理")
public class AdminTagController {

    @Resource
    private TagService tagService;

    /**
     * 新增标签
     */
    @PostMapping
    @Log(module = "标签管理", type = "新增", desc = "管理员创建了新标签")
    @Operation(summary = "新增标签")
    public Result<Void> addTag(@Valid @RequestBody TagAddDTO addDTO) {
        tagService.addTag(addDTO);
        return Result.success();
    }

    /**
     * 更新标签
     */
    @PutMapping
    @Log(module = "标签管理", type = "修改", desc = "管理员更新了标签信息")
    @Operation(summary = "更新标签")
    public Result<Void> updateTag(@Valid @RequestBody TagUpdateDTO updateDTO) {
        tagService.updateTag(updateDTO);
        return Result.success();
    }

    /**
     * 删除指定标签
     */
    @DeleteMapping("/{id}")
    @Log(module = "标签管理", type = "删除", desc = "管理员删除了指定标签")
    @Operation(summary = "删除标签",description = "删除标签会同步解除该标签与文章的关联关系（ArticleTag中间表），但不会删除文章本体。")
    public Result<Void> deleteTag(@PathVariable @Positive(message = "标签ID非法") Long id) {
        tagService.deleteTagById(id);
        return Result.success();
    }

    /**
     * 批量删除标签
     */
    @DeleteMapping("/batch")
    @Log(module = "标签管理", type = "批量删除", desc = "管理员批量移除了多个标签")
    @Operation(summary = "批量删除标签")
    public Result<Void> batchDeleteTags(
            @RequestBody
            @NotEmpty(message = "请选择要删除的标签")
            List<
                    @NotNull(message = "ID不能为null")
                    @Positive(message = "ID必须为正数")
                            Long
                    > ids
    ) {
        tagService.batchDeleteTags(ids);
        return Result.success();
    }

    /**
     * 分页查询标签列表
     */
    @GetMapping
    @Operation(summary = "分页查询标签列表")
    public Result<IPage<TagVO>> pageTags(@Valid TagQueryDTO queryDTO) {
        IPage<TagVO> pageResult = tagService.pageAdminTags(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 获取单条标签详情（用于编辑回显）
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取标签详情(编辑用)")
    public Result<TagVO> getTagById(@PathVariable @Positive(message = "标签ID非法") Long id) {
        TagVO vo = tagService.getTagById(id);
        return Result.success(vo);
    }

    /**
     * 获取所有标签（下拉列表用）
     */
    @GetMapping("/options")
    @Operation(summary = "获取所有标签", description = "获取全量标签数据，通常用于文章发布页的标签选择器。")
    public Result<List<TagVO>> listAllTags() {
        List<TagVO> tags = tagService.listAdminTags();
        return Result.success(tags);
    }
}