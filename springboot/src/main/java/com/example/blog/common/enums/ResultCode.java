package com.example.blog.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API 响应状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /* 成功状态 */
    SUCCESS(200, "操作成功"),

    /* 客户端错误 (400-499) */
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权，请登录"),
    FORBIDDEN(403, "拒绝访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "资源冲突"),

    /* 服务端错误 (500-599) */
    INTERNAL_SERVER_ERROR(500, "系统内部错误"),
    NOT_IMPLEMENTED(501, "接口未实现");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 默认提示信息
     */
    private final String message;

}
