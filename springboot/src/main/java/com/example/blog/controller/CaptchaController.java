package com.example.blog.controller; // 替换为你实际的 controller 包路径

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.example.blog.utils.IpUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 专门为 Spring Boot 3 适配的验证码接口
 */
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Resource
    private CaptchaService captchaService;

    /**
     * 获取验证码图、坐标等
     */
    @PostMapping("/get")
    public ResponseModel get(@RequestBody CaptchaVO data, HttpServletRequest request) {
        // 将用户的 IP 作为验证码的安全标识（BrowserInfo）传入
        data.setBrowserInfo(IpUtils.getIpAddr(request));
        return captchaService.get(data);
    }

    /**
     * 前端滑动后核对轨迹
     */
    @PostMapping("/check")
    public ResponseModel check(@RequestBody CaptchaVO data, HttpServletRequest request) {
        data.setBrowserInfo(IpUtils.getIpAddr(request));
        return captchaService.check(data);
    }
}