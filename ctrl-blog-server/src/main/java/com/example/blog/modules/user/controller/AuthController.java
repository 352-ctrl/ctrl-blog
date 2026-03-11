package com.example.blog.modules.user.controller;

import com.example.blog.common.base.Result;
import com.example.blog.common.utils.SecurityUtils;
import com.example.blog.core.annotation.RateLimit;
import com.example.blog.core.annotation.VerifyCaptcha;
import com.example.blog.modules.operation.model.dto.EmailRequestDTO;
import com.example.blog.modules.user.model.dto.UserForgotPwdDTO;
import com.example.blog.modules.user.model.dto.UserLoginDTO;
import com.example.blog.modules.user.model.dto.UserRegisterDTO;
import com.example.blog.modules.user.model.vo.UserLoginVO;
import com.example.blog.modules.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 认证中心控制器
 * 处理用户登录、注册、验证码发送等安全操作
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "前台/认证中心")
public class AuthController {

    @Resource
    private AuthService authService;

    /**
     * 用户登录 (获取Token)
     */
    @PostMapping("/token")
    @VerifyCaptcha
    @RateLimit(key = "ip", time = 60, count = 10)
    @Operation(summary = "用户登录", description = "校验用户名密码。成功后返回 **JWT Token**。<br>后续请求建议使用标准格式，在请求头中携带：`Authorization: Bearer {token}`。")
    public Result<UserLoginVO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        UserLoginVO loginVO = authService.login(loginDTO);
        return Result.success(loginVO);
    }

    /**
     * 用户注册 (创建账户)
     */
    @PostMapping("/accounts")
    @RateLimit(key = "ip", time = 60, count = 5)
    @Operation(summary = "用户注册", description = "新用户注册。系统会自动校验用户名重复性，并加密存储密码。")
    public Result<UserLoginVO> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        UserLoginVO loginVO = authService.register(registerDTO);
        return Result.success(loginVO);
    }

    /**
     * 发送注册邮箱验证码
     */
    @PostMapping("/register/verification-codes")
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
    @PostMapping("/password-reset/verification-codes")
    @VerifyCaptcha
    @RateLimit(key = "ip", time = 60, count = 1)
    @Operation(summary = "发送找回密码邮箱验证码", description = "用于前台用户忘记密码时找回。验证码有效期为 5 分钟，1分钟内防刷限制。")
    public Result<Void> sendForgotPwdEmailCode(@Valid @RequestBody EmailRequestDTO emailDTO) {
        authService.sendForgotPwdEmailCode(emailDTO);
        return Result.success();
    }

    /**
     * 通过邮箱验证码重置密码 (更新密码资源)
     */
    @PutMapping("/password")
    @RateLimit(key = "ip", time = 60, count = 5)
    @Operation(summary = "通过邮箱验证码重置密码", description = "前台用户忘记密码后，凭借邮箱验证码设置新密码。")
    public Result<Void> resetPasswordByEmail(@Valid @RequestBody UserForgotPwdDTO forgotPwdDTO) {
        authService.resetPasswordByEmail(forgotPwdDTO);
        return Result.success();
    }

    /**
     * 退出登录 (销毁Token)
     */
    @Operation(summary = "退出登录", description = "销毁当前会话，将 Token 加入黑名单。")
    @DeleteMapping("/token")
    public Result<Void> logout(HttpServletRequest request) {
        String token = SecurityUtils.resolveToken(request);

        authService.logout(token);
        return Result.success();
    }

}