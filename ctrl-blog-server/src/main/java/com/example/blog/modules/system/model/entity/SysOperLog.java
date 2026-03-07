package com.example.blog.modules.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.blog.common.base.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 系统操作日志实体类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_oper_log")
public class SysOperLog extends BaseEntity {

    /**
     * 操作人ID
     */
    private Long userId;

    /**
     * 操作人昵称
     * 冗余字段，方便列表展示
     */
    private String nickname;

    /**
     * 操作IP地址
     */
    private String ip;

    /**
     * 用户代理 (User-Agent)
     */
    private String userAgent;

    /**
     * 操作模块
     * 示例：文章管理、系统用户、角色管理
     */
    private String module;

    /**
     * 操作类型
     * 示例：新增、修改、删除、导出
     */
    private String type;

    /**
     * HTTP请求方式
     * 示例：GET、POST、PUT、DELETE
     */
    private String requestMethod;

    /**
     * 操作描述
     * 详细说明做了什么操作
     */
    private String description;

    /**
     * 请求方法
     * 格式：全限定类名.方法名()
     */
    private String method;

    /**
     * 请求参数
     * 通常存储为 JSON 字符串
     */
    private String params;

    /**
     * 返回结果 / 异常信息
     * 存储操作的返回值或抛出的异常堆栈
     */
    private String result;

    /**
     * 执行耗时 (毫秒)
     */
    private Integer costTime;

    /**
     * 操作状态
     * 1: 成功
     * 0: 失败
     */
    private Integer status;

}
