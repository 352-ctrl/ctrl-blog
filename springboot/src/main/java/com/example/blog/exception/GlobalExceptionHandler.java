package com.example.blog.exception;

import com.example.blog.common.Result;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.enums.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
        // 使用枚举 + 自定义消息
        return Result.error(ResultCode.PARAM_ERROR, errorMsg);
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
        String invalidValue = Objects.toString(e.getValue(), "");
        String requiredType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "Unknown";

        String errorMsg = String.format("参数[%s]格式错误，期望类型为[%s]，实际传入[%s]",
                paramName, requiredType, invalidValue);

        log.warn("参数类型转换失败：{}", errorMsg);
        return Result.error(ResultCode.PARAM_ERROR, errorMsg);
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