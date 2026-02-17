package com.example.blog.interceptor;

import com.example.blog.service.VisitService;
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
        if("GET".equalsIgnoreCase(request.getMethod())){
            visitService.recordVisit();
        }
        return true;
    }
}