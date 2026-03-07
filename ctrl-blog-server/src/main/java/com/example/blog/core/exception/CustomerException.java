package com.example.blog.core.exception;

import com.example.blog.common.enums.ResultCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 自定义业务异常
 * 用于业务逻辑错误处理，区别于系统异常
 */
@Getter
@Setter
public class CustomerException extends RuntimeException {

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误消息
     */
    private String msg;

    /**
     * 构造函数（使用错误枚举）
     *
     * @param resultCode 错误枚举
     */
    public CustomerException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.msg = resultCode.getMessage();
    }

    /**
     * 构造函数（指定错误码和消息）
     *
     * @param resultCode 错误枚举
     * @param msg 错误消息
     */
    public CustomerException(ResultCode resultCode, String msg) {
        super(msg);
        this.code = resultCode.getCode();
        this.msg = msg;
    }

    /**
     * 构造函数（默认错误码，指定消息）
     *
     * @param msg 错误消息
     */
    public CustomerException(String msg) {
        super(msg);
        this.code = ResultCode.INTERNAL_SERVER_ERROR.getCode();
        this.msg = msg;
    }

    /**
     * 无参构造函数
     */
    public CustomerException() {
        super(ResultCode.INTERNAL_SERVER_ERROR.getMessage());
        this.code = ResultCode.INTERNAL_SERVER_ERROR.getCode();
        this.msg = ResultCode.INTERNAL_SERVER_ERROR.getMessage();
    }
}