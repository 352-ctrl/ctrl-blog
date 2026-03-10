package com.example.blog.core.exception;

import cn.hutool.core.util.StrUtil;
import com.example.blog.common.base.Result;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.enums.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Objects;

/**
 * 全局异常处理器
 * 统一处理控制器层抛出的异常，返回标准化错误响应
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 日志记录器
     */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理 404 异常 (Spring Boot 3.x 特性)
     * 避免 404 错误被当成系统异常打印长串堆栈
     *
     * @param e 404异常对象
     * @return 统一错误响应 (404)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("404 接口不存在: {}", e.getResourcePath());
        return Result.error(ResultCode.NOT_FOUND, MessageConstants.MSG_RESOURCE_NOT_FOUND);
    }

    /**
     * 处理参数校验异常 (Spring Validation)
     * 例如：@NotNull, @NotBlank 校验失败
     *
     * @param e 参数校验异常对象
     * @return 统一错误响应 (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String errorMsg = ResultCode.PARAM_ERROR.getMessage(); // 默认消息

        // 提取第一个字段的错误提示
        FieldError fieldError = bindingResult.getFieldError();
        if (fieldError != null) {
            errorMsg = fieldError.getDefaultMessage();
        }

        log.warn("参数校验失败：{}", errorMsg);
        return Result.error(ResultCode.PARAM_ERROR, errorMsg);
    }

    /**
     * 处理 HTTP 消息不可读异常 (JSON 反序列化异常)
     * 例如：前端传了不存在的枚举值，或者 JSON 格式完全错误
     *
     * @param e 异常对象
     * @return 统一错误响应 (400)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("参数解析失败 (JSON反序列化异常): {}", e.getMessage());
        return Result.error(ResultCode.PARAM_ERROR, MessageConstants.MSG_PARAM_FORMAT_ERROR);
    }

    /**
     * 处理参数类型转换异常
     * 例如：API需要Long类型，前端传了String且无法解析
     *
     * @param e 参数类型转换异常对象
     * @return 统一错误响应 (400)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String paramName = e.getName();
        String invalidValue = Objects.toString(e.getValue(), StrUtil.EMPTY);
        String requiredType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : Constants.UNKNOWN;

        String errorMsg = String.format(MessageConstants.MSG_PARAM_TYPE_MISMATCH,
                paramName, requiredType, invalidValue);

        log.warn("参数类型转换失败：{}", errorMsg);
        return Result.error(ResultCode.PARAM_ERROR, errorMsg);
    }

    /**
     * 处理文件上传异常 (请求格式非 multipart/form-data)
     *
     * @param e 文件上传异常对象
     * @return 统一错误响应 (400)
     */
    @ExceptionHandler(org.springframework.web.multipart.MultipartException.class)
    public Result<Void> handleMultipartException(org.springframework.web.multipart.MultipartException e) {
        log.warn("文件上传失败：当前请求不是 multipart/form-data 格式");
        return Result.error(ResultCode.PARAM_ERROR, MessageConstants.MSG_UPLOAD_FORMAT_ERROR);
    }

    /**
     * 处理自定义业务异常
     * 业务逻辑中手动抛出的异常
     *
     * @param e 自定义异常对象
     * @return 统一错误响应
     */
    @ExceptionHandler(CustomerException.class)
    public Result<Void> customerError(CustomerException e) {
        log.warn("业务异常：code={}, msg={}", e.getCode(), e.getMsg());
        return Result.error(e.getCode(), e.getMsg());
    }

    /**
     * 处理系统兜底异常
     * 所有未捕获的异常都会走到这里
     *
     * @param e 异常对象
     * @return 统一错误响应 (500)
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> error(Exception e) {
        log.error("系统异常", e);
        return Result.error(ResultCode.INTERNAL_SERVER_ERROR, MessageConstants.MSG_SYSTEM_ERROR);
    }
}