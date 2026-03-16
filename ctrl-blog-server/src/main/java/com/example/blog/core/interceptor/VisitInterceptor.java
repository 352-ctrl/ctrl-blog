package com.example.blog.core.interceptor;

import com.example.blog.common.utils.IpUtils;
import com.example.blog.modules.monitor.service.VisitService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class VisitInterceptor implements HandlerInterceptor {

    @Resource
    private VisitService visitService; // 注入接口

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 只统计 GET 请求的页面访问
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            // 1. 获取客户端真实 IP
            String ip = IpUtils.getIpAddr(request);
            // 2. 传入 IP 进行冷却期防刷记录
            visitService.recordVisit(ip);
        }
        return true;
    }
}