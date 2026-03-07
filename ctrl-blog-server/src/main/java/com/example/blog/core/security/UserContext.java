package com.example.blog.core.security;

import com.example.blog.modules.user.model.dto.UserPayloadDTO;

/**
 * 用户上下文工具类
 * 基于 ThreadLocal 实现，用于在当前线程中存储和获取用户信息
 */
public class UserContext {

    // 创建一个线程局部的容器，专门存放 UserPayloadDTO
    private static final ThreadLocal<UserPayloadDTO> USER_HOLDER = new ThreadLocal<>();

    /**
     * 存储用户信息
     */
    public static void set(UserPayloadDTO user) {
        USER_HOLDER.set(user);
    }

    /**
     * 获取用户信息
     */
    public static UserPayloadDTO get() {
        return USER_HOLDER.get();
    }

    /**
     * 清除用户信息（防止内存泄漏，必须调用）
     */
    public static void remove() {
        USER_HOLDER.remove();
    }
}