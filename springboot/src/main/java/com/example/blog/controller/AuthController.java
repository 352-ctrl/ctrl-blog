package com.example.blog.controller;

import cn.hutool.core.util.StrUtil;
import com.example.blog.annotation.Log;
import com.example.blog.annotation.RateLimit;
import com.example.blog.common.Result;
import com.example.blog.common.constants.Constants;
import com.example.blog.dto.EmailRequestDTO;
import com.example.blog.dto.user.UserLoginDTO;
import com.example.blog.dto.user.UserRegisterDTO;
import com.example.blog.service.AuthService;
import com.example.blog.vo.UserLoginVO;
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
    @Log(module = "认证中心", type = "登录", desc = "用户执行了登录操作")
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
    @Log(module = "认证中心", type = "注册", desc = "新用户注册账号")
    @Operation(summary = "用户注册", description = "新用户注册。系统会自动校验用户名重复性，并加密存储密码。")
    public Result<Void> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        authService.register(registerDTO);
        return Result.success();
    }

    /**
     * 发送邮箱验证码
     * 安全提示：此接口极易被恶意刷量，RateLimit 必须严格
     */
    @PostMapping("/email/code")
    @RateLimit(key = "ip", time = 60, count = 1)
    @Log(module = "认证中心", type = "邮件", desc = "请求发送邮箱验证码")
    @Operation(summary = "发送邮箱验证码", description = "用于注册或找回密码。验证码有效期通常为 5-10 分钟。")
    public Result<Void> sendEmailCode(@Valid @RequestBody EmailRequestDTO emailDTO) {
        authService.sendEmailCode(emailDTO);
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