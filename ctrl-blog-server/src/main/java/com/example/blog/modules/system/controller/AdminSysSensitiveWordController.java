package com.example.blog.modules.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.core.annotation.Log;
import com.example.blog.common.base.Result;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.system.model.dto.SysSensitiveWordAddDTO;
import com.example.blog.modules.system.model.dto.SysSensitiveWordQueryDTO;
import com.example.blog.modules.system.model.dto.SysSensitiveWordUpdateDTO;
import com.example.blog.modules.system.service.SysSensitiveWordService;
import com.example.blog.modules.system.model.vo.SysSensitiveWordVO;
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
 * 后台敏感词管理控制器
 * 提供敏感词的增删改查 RESTful API 接口
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/sensitive-words")
@AuthCheck(role = BizStatus.ROLE_ADMIN)
@Tag(name = "后台/敏感词管理", description = "系统敏感词库的维护接口")
public class AdminSysSensitiveWordController {

    @Resource
    private SysSensitiveWordService sysSensitiveWordService;

    /**
     * 新增敏感词
     */
    @PostMapping
    @Log(module = "敏感词管理", type = "新增", desc = "管理员添加了新的敏感词")
    @Operation(summary = "新增敏感词")
    public Result<Void> addSensitiveWord(@Valid @RequestBody SysSensitiveWordAddDTO addDTO) {
        sysSensitiveWordService.addSensitiveWord(addDTO);
        return Result.success();
    }

    /**
     * 更新敏感词
     */
    @PutMapping
    @Log(module = "敏感词管理", type = "修改", desc = "管理员更新了敏感词信息")
    @Operation(summary = "更新敏感词")
    public Result<Void> updateSensitiveWord(@Valid @RequestBody SysSensitiveWordUpdateDTO updateDTO) {
        sysSensitiveWordService.updateSensitiveWord(updateDTO);
        return Result.success();
    }

    /**
     * 删除指定敏感词
     */
    @DeleteMapping("/{id}")
    @Log(module = "敏感词管理", type = "删除", desc = "管理员删除了指定敏感词")
    @Operation(summary = "删除敏感词")
    public Result<Void> deleteSensitiveWord(@PathVariable @Positive(message = "敏感词ID非法") Long id) {
        sysSensitiveWordService.deleteSensitiveWordById(id);
        return Result.success();
    }

    /**
     * 批量删除敏感词
     */
    @DeleteMapping("/batch")
    @Log(module = "敏感词管理", type = "批量删除", desc = "管理员批量移除了多个敏感词")
    @Operation(summary = "批量删除敏感词")
    public Result<Void> batchDeleteSensitiveWords(
            @RequestBody
            @NotEmpty(message = "请选择要删除的敏感词")
            List<
                    @NotNull(message = "ID不能为null")
                    @Positive(message = "ID必须为正数")
                            Long
                    > ids
    ) {
        sysSensitiveWordService.batchDeleteSensitiveWords(ids);
        return Result.success();
    }

    /**
     * 分页查询敏感词列表
     */
    @GetMapping
    @Operation(summary = "分页查询敏感词列表", description = "支持按敏感词内容模糊查询")
    public Result<IPage<SysSensitiveWordVO>> pageSensitiveWords(@Valid SysSensitiveWordQueryDTO queryDTO) {
        IPage<SysSensitiveWordVO> pageResult = sysSensitiveWordService.pageAdminSensitiveWords(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 获取单条敏感词详情（用于编辑回显）
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取敏感词详情(编辑用)")
    public Result<SysSensitiveWordVO> getSensitiveWordById(@PathVariable @Positive(message = "敏感词ID非法") Long id) {
        SysSensitiveWordVO vo = sysSensitiveWordService.getSensitiveWordById(id);
        return Result.success(vo);
    }
}