package com.example.blog.modules.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.core.annotation.Log;
import com.example.blog.common.base.Result;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.user.model.dto.AdminResetPwdDTO;
import com.example.blog.modules.user.model.dto.UserAddDTO;
import com.example.blog.modules.user.model.dto.UserQueryDTO;
import com.example.blog.modules.user.model.dto.UserUpdateDTO;
import com.example.blog.modules.user.service.UserService;
import com.example.blog.modules.user.model.vo.UserVO;
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
 * 系统用户管理控制器
 * 提供用户的增删改查 RESTful API 接口
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/users")
@AuthCheck(role = BizStatus.ROLE_ADMIN)
@Tag(name = "后台/用户管理")
public class AdminUserController {

    @Resource
    private UserService userService;

    /**
     * 新增用户
     */
    @PostMapping
    @Log(module = "用户管理", type = "新增", desc = "管理员创建了新用户")
    @Operation(summary = "新增用户")
    public Result<Void> addUser(@Valid @RequestBody UserAddDTO addDTO) {
        userService.addUser(addDTO);
        return Result.success();
    }

    /**
     * 更新用户
     */
    @PutMapping
    @Log(module = "用户管理", type = "修改", desc = "管理员更新了用户信息")
    @Operation(summary = "更新用户")
    public Result<Void> updateUser(@Valid @RequestBody UserUpdateDTO updateDTO) {
        userService.updateUser(updateDTO);
        return Result.success();
    }

    /**
     * 管理员重置用户密码
     */
    @PutMapping("/password")
    @Log(module = "用户管理", type = "重置密码", desc = "管理员重置了用户密码")
    @Operation(summary = "重置用户密码", description = "管理员强制重置指定用户的密码。具有防越权校验。")
    public Result<Void> resetPassword(@Valid @RequestBody AdminResetPwdDTO resetPwdDTO) {
        userService.resetPasswordByAdmin(resetPwdDTO);
        return Result.success();
    }

    /**
     * 删除指定用户
     */
    @DeleteMapping("/{id}")
    @Log(module = "用户管理", type = "删除", desc = "管理员删除了单个用户")
    @Operation(summary = "删除用户", description = "逻辑删除用户。**注意：** 无法删除超级管理员或当前登录用户自己。")
    public Result<Void> deleteUser(@PathVariable @Positive(message = "用户ID非法") Long id) {
        userService.deleteUserById(id);
        return Result.success();
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    @Log(module = "用户管理", type = "批量删除", desc = "管理员批量删除了用户")
    @Operation(summary = "批量删除用户")
    public Result<Void> batchDeleteUsers(
            @RequestBody
            @NotEmpty(message = "请选择要删除的用户")
            List<
                    @NotNull(message = "ID不能为null")
                    @Positive(message = "ID必须为正数")
                            Long
                    > ids
    ) {
        userService.batchDeleteUsers(ids);
        return Result.success();
    }

    /**
     * 获取单条用户详情（用于编辑回显）
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情(编辑用)")
    public Result<UserVO> getUserById(@PathVariable @Positive(message = "用户ID非法") Long id) {
        UserVO vo = userService.getUserById(id);
        return Result.success(vo);
    }

    /**
     * 分页查询用户列表
     */
    @GetMapping
    @Operation(summary = "分页查询用户列表")
    public Result<IPage<UserVO>> pageUsers(@Valid UserQueryDTO queryDTO) {
        IPage<UserVO> pageResult = userService.pageAdminUsers(queryDTO);
        return Result.success(pageResult);
    }
}