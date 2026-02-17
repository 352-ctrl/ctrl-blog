package com.example.blog.service;

import com.example.blog.dto.EmailRequestDTO;
import com.example.blog.dto.user.UserLoginDTO;
import com.example.blog.dto.user.UserRegisterDTO;
import com.example.blog.vo.UserLoginVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 发送邮箱验证码
     * <p>包含防刷校验（1分钟内只能发一次）和邮箱查重逻辑</p>
     *
     * @param emailRequestDTO 邮箱请求DTO
     */
    void sendEmailCode(EmailRequestDTO emailRequestDTO);

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
    void register(UserRegisterDTO userRegisterDTO);

    /**
     * 用户注销登录
     * @param token 当前请求携带的令牌
     */
    void logout(String token);
}