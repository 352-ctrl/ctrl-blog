package com.example.blog.modules.user.service;

import com.example.blog.modules.operation.model.dto.EmailRequestDTO;
import com.example.blog.modules.user.model.dto.UserForgotPwdDTO;
import com.example.blog.modules.user.model.dto.UserLoginDTO;
import com.example.blog.modules.user.model.dto.UserRegisterDTO;
import com.example.blog.modules.user.model.vo.UserLoginVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 发送邮箱注册验证码
     * <p>包含防刷校验（1分钟内只能发一次）和邮箱查重逻辑</p>
     *
     * @param emailRequestDTO 邮箱请求DTO
     */
    void sendRegisterEmailCode(EmailRequestDTO emailRequestDTO);

    /**
     * 发送邮箱忘记密码验证码
     * <p>包含防刷校验（1分钟内只能发一次）和邮箱查重逻辑</p>
     *
     * @param emailRequestDTO 邮箱请求DTO
     */
    void sendForgotPwdEmailCode(EmailRequestDTO emailRequestDTO);

    /**
     * 前台用户：通过邮箱验证码重置密码
     * @param forgotPwdDTO 忘记密码参数
     */
    void resetPasswordByEmail(UserForgotPwdDTO forgotPwdDTO);

    /**
     * 用户登录
     * @param userLoginDTO 用户登录DTO（用户名和密码）
     * @return 包含用户信息和token的VO
     */
    UserLoginVO login(UserLoginDTO userLoginDTO);

    /**
     * 用户注册
     * @param userRegisterDTO 用户注册DTO
     */
    UserLoginVO register(UserRegisterDTO userRegisterDTO);

    /**
     * 用户注销登录
     * @param token 当前请求携带的令牌
     */
    void logout(String token);

    /**
     * 根据用户ID强制用户下线 (拉黑当前 Token 并清除缓存)
     * @param userId 目标用户ID
     */
    void forceLogoutByUserId(Long userId);
}