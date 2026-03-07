package com.example.blog.core.aspect;

import cn.hutool.core.util.StrUtil;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.core.exception.CustomerException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 滑动验证码统一校验切面
 */
@Slf4j
@Aspect
@Component
public class CaptchaAspect {

    @Resource
    private CaptchaService captchaService;

    /**
     * 拦截所有标注了 @VerifyCaptcha 注解的方法
     */
    @Before("@annotation(com.example.blog.core.annotation.VerifyCaptcha)")
    public void doBefore(JoinPoint joinPoint) {
        String captchaVerification = getCaptchaVerification(joinPoint);

        // 1. 校验是否为空
        if (StrUtil.isBlank(captchaVerification)) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_CAPTCHA_REQUIRE);
        }

        // 2. 调用 Anji-Captcha 组件进行二次校验
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaVerification(captchaVerification);
        ResponseModel response = captchaService.verification(captchaVO);

        // 3. 校验失败，抛出业务异常阻断请求
        if (!response.isSuccess()) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_CAPTCHA_VERIFY_FAILED);
        }
    }

    /**
     * 核心：三级参数降级寻找策略
     * 尝试从 Header -> Parameter 中提取 captchaVerification
     */
    private String getCaptchaVerification(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // 级别 1：尝试从 HTTP Header 中获取
            String verification = request.getHeader("captchaVerification");
            if (StrUtil.isNotBlank(verification)) {
                return verification;
            }

            // 级别 2：尝试从 URL Query Parameter 或 Form 表单中获取
            verification = request.getParameter("captchaVerification");
            if (StrUtil.isNotBlank(verification)) {
                return verification;
            }
        }

        return null;
    }
}