package com.example.blog.controller;

import com.example.blog.annotation.AuthCheck;
import com.example.blog.annotation.Log;
import com.example.blog.annotation.RateLimit;
import com.example.blog.common.Result;
import com.example.blog.common.constants.Constants;
import com.example.blog.dto.user.UserChangePwdDTO;
import com.example.blog.dto.user.UserProfileUpdateDTO;
import com.example.blog.service.UserProfileService;
import com.example.blog.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 个人中心控制器
 * 处理当前登录用户的个人信息查询、修改及安全设置
 */
@RestController
@RequestMapping("/api/user")
@AuthCheck
@Tag(name = "个人中心")
public class UserProfileController {

    @Resource
    private UserProfileService userProfileService;

    /**
     * 获取当前用户信息
     */
    @GetMapping
    @Operation(summary = "获取个人信息", description = "获取当前登录用户的详细档案信息（昵称、头像、邮箱等）。")
    public Result<UserVO> getProfile() {
        UserVO userVO = userProfileService.getProfile();
        return Result.success(userVO);
    }

    /**
     * 修改个人信息
     */
    @PutMapping
    @RateLimit(key = "ip", time = 60, count = 5)
    @Log(module = "个人中心", type = "修改", desc = "用户修改了个人信息")
    @Operation(summary = "修改个人信息", description = "更新昵称、头像、简介等基本资料。")
    public Result<Void> updateProfile(@Valid @RequestBody UserProfileUpdateDTO updateDTO) {
        userProfileService.updateProfile(updateDTO);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    @RateLimit(key = "ip", time = 60, count = 3) // 关键安全接口，限流严格一点
    @Log(module = "个人中心", type = "安全", desc = "用户执行了修改密码操作")
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码。修改成功后通常建议引导用户重新登录。")
    public Result<Void> changePassword(@Valid @RequestBody UserChangePwdDTO pwdDTO, HttpServletRequest request) {
        // 从 Header 获取 Token
        String token = request.getHeader(Constants.HEADER_TOKEN);
        userProfileService.changePassword(pwdDTO, token);
        return Result.success();
    }

}