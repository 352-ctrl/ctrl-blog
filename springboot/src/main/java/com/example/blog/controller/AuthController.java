package com.example.blog.controller;

import cn.hutool.core.util.StrUtil;
import com.example.blog.annotation.RateLimit;
import com.example.blog.annotation.VerifyCaptcha;
import com.example.blog.common.Result;
import com.example.blog.common.constants.Constants;
import com.example.blog.dto.EmailRequestDTO;
import com.example.blog.dto.user.UserForgotPwdDTO;
import com.example.blog.dto.user.UserLoginDTO;
import com.example.blog.dto.user.UserRegisterDTO;
import com.example.blog.service.AuthService;
import com.example.blog.vo.user.UserLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证中心控制器
 * 处理用户登录、注册、验证码发送等安全操作
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证中心")
public class AuthController {

    @Resource
    private AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @RateLimit(key = "ip", time = 60, count = 10)
    @Operation(summary = "用户登录", description = "校验用户名密码。成功后返回 **Token**。<br>后续请求需在 Header 中携带 `Authorization: Bearer {token}`。")
    public Result<UserLoginVO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        UserLoginVO loginVO = authService.login(loginDTO);
        return Result.success(loginVO);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @RateLimit(key = "ip", time = 60, count = 5)
    @Operation(summary = "用户注册", description = "新用户注册。系统会自动校验用户名重复性，并加密存储密码。")
    public Result<UserLoginVO> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        UserLoginVO loginVO = authService.register(registerDTO);
        return Result.success(loginVO);
    }

    /**
     * 发送注册邮箱验证码
     */
    @PostMapping("/email/code/register")
    @VerifyCaptcha
    @RateLimit(key = "ip", time = 60, count = 1)
    @Operation(summary = "发送注册邮箱验证码", description = "用于新用户注册。验证码有效期为 5 分钟，1分钟内防刷限制。")
    public Result<Void> sendRegisterEmailCode(@Valid @RequestBody EmailRequestDTO emailDTO) {
        authService.sendRegisterEmailCode(emailDTO);
        return Result.success();
    }

    /**
     * 发送找回密码邮箱验证码
     */
    @PostMapping("/email/code/forgot")
    @VerifyCaptcha
    @RateLimit(key = "ip", time = 60, count = 1)
    @Operation(summary = "发送找回密码邮箱验证码", description = "用于前台用户忘记密码时找回。验证码有效期为 5 分钟，1分钟内防刷限制。")
    public Result<Void> sendForgotPwdEmailCode(@Valid @RequestBody EmailRequestDTO emailDTO) {
        authService.sendForgotPwdEmailCode(emailDTO);
        return Result.success();
    }

    /**
     * 通过邮箱验证码重置密码
     */
    @PostMapping("/password/reset")
    @RateLimit(key = "ip", time = 60, count = 5)
    @Operation(summary = "通过邮箱验证码重置密码", description = "前台用户忘记密码后，凭借邮箱验证码设置新密码。")
    public Result<Void> resetPasswordByEmail(@Valid @RequestBody UserForgotPwdDTO forgotPwdDTO) {
        authService.resetPasswordByEmail(forgotPwdDTO);
        return Result.success();
    }

    /**
     * 退出登录
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        // 从 Header 中获取 token
        String token = request.getHeader(Constants.HEADER_TOKEN);

        // 如果 Header 为空，尝试从参数获取
        if (StrUtil.isBlank(token)) {
            token = request.getParameter(Constants.HEADER_TOKEN);
        }

        authService.logout(token);
        return Result.success();
    }

}