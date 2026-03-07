package com.example.blog.common.base;

import com.example.blog.common.enums.ResultCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一API响应结果封装
 * 标准化API响应格式，包含状态码、消息和数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一API响应结果")
public class Result<T> {

    @Schema(description = "状态码", example = "200")
    private Integer code;

    @Schema(description = "响应消息", example = "请求成功")
    private String msg;

    @Schema(description = "响应数据")
    private T data;

    // ============================ 成功响应 ============================

    /**
     * 创建成功响应（无数据）
     *
     * @return 成功响应结果
     */
    public static <E> Result<E> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 创建成功响应（含数据）
     *
     * @param data 响应数据
     * @return 成功响应结果
     */
    public static <E> Result<E> success(E data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    // ============================ 错误响应 ============================

    /**
     * 创建错误响应（使用枚举状态码）
     *
     * @param resultCode 错误枚举
     * @return 错误响应结果
     */
    public static <E> Result<E> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 创建错误响应（使用枚举 + 自定义消息）
     *
     * @param resultCode 错误枚举
     * @param msg        自定义错误消息
     * @return 错误响应结果
     */
    public static <E> Result<E> error(ResultCode resultCode, String msg) {
        return new Result<>(resultCode.getCode(), msg, null);
    }

    /**
     * 创建错误响应（默认系统错误码）
     *
     * @param msg 错误消息
     * @return 错误响应结果
     */
    public static <E> Result<E> error(String msg) {
        return error(ResultCode.INTERNAL_SERVER_ERROR, msg);
    }

    /**
     * 创建错误响应（自定义错误码）
     *
     * @param code 错误码
     * @param msg 错误消息
     * @return 错误响应结果
     */
    public static <E> Result<E> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }
}