package com.example.blog.modules.user.controller;

import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.core.annotation.Log;
import com.example.blog.core.annotation.RateLimit;
import com.example.blog.core.annotation.VerifyCaptcha;
import com.example.blog.common.base.Result;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.operation.model.dto.EmailRequestDTO;
import com.example.blog.modules.user.model.dto.UserChangeEmailDTO;
import com.example.blog.modules.user.model.dto.UserChangePwdDTO;
import com.example.blog.modules.user.model.dto.UserProfileUpdateDTO;
import com.example.blog.modules.user.service.UserProfileService;
import com.example.blog.modules.user.model.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 后台后台个人中心控制器
 * 处理当前登录用户的个人信息查询、修改及安全设置
 */
@RestController
@RequestMapping("/api/v1/admin/profile")
@AuthCheck(role = BizStatus.ROLE_ADMIN)
@Tag(name = "后台/个人中心")
public class AdminUserProfileController {

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

    @PostMapping("/email/verification-codes")
    @VerifyCaptcha
    @RateLimit(key = "ip", time = 60, count = 1)
    @Operation(summary = "发送换绑邮箱验证码", description = "向新邮箱发送验证码，需通过行为验证码校验。")
    public Result<Void> sendBindEmailCode(@Valid @RequestBody EmailRequestDTO emailRequestDTO) {
        userProfileService.sendBindEmailCode(emailRequestDTO);
        return Result.success();
    }

    /**
     * 确认更换邮箱
     */
    @PutMapping("/email")
    @RateLimit(key = "ip", time = 60, count = 3)
    @Log(module = "个人中心", type = "安全", desc = "用户执行了换绑邮箱操作")
    @Operation(summary = "更换绑定邮箱", description = "校验验证码并换绑。换绑成功后当前账号会被强制下线。")
    public Result<Void> changeEmail(@Valid @RequestBody UserChangeEmailDTO changeEmailDTO, HttpServletRequest request) {
        // 从 Header 获取 Token，用于强制下线
        String token = request.getHeader(Constants.HEADER_TOKEN);
        userProfileService.changeEmail(changeEmailDTO, token);
        return Result.success();
    }
}