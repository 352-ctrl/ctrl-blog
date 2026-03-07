package com.example.blog.modules.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.blog.common.base.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 系统登录日志实体类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_login_log")
public class SysLoginLog extends BaseEntity {

    /**
     * 账号邮箱
     * 记录用户尝试登录时输入的邮箱
     */
    private String email;

    /**
     * 登录IP地址
     */
    private String ip;

    /**
     * 登录地点
     * 可通过 IP 转换工具解析为“上海市-浦东新区”
     */
    private String location;

    /**
     * 浏览器类型
     * 从 User-Agent 中解析出的浏览器名称，如 Chrome, Firefox
     */
    private String browser;

    /**
     * 操作系统
     * 从 User-Agent 中解析出的系统名称，如 Windows 11, macOS, Android
     */
    private String os;

    /**
     * 登录状态
     * 1: 成功
     * 0: 失败
     */
    private Integer status;

    /**
     * 提示消息
     * 记录登录成功，或失败的原因（如：密码错误、验证码过期、用户被封禁等）
     */
    private String message;

}