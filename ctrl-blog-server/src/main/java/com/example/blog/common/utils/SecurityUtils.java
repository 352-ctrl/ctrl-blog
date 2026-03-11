package com.example.blog.common.utils;

import cn.hutool.core.util.StrUtil;
import com.example.blog.common.constants.Constants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

public class SecurityUtils {
    /**
     * 从请求中解析 Token
     */
    public static String resolveToken(HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StrUtil.isNotBlank(token) && token.startsWith(Constants.TOKEN_PREFIX)) {
            return token.substring(Constants.TOKEN_PREFIX.length()).trim();
        }
        token = request.getHeader(Constants.HEADER_TOKEN);
        if (StrUtil.isBlank(token)) {
            token = request.getParameter(Constants.HEADER_TOKEN);
        }
        return token;
    }
}
