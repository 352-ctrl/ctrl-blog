package com.example.blog.utils;

import cn.hutool.core.util.StrUtil;
import com.example.blog.common.constants.Constants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP地址获取工具类
 */
@Slf4j
public class IpUtils {

    /**
     * 获取客户端真实IP地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return Constants.IP_UNKNOWN;
        }

        String ip = null;

        // 1. 循环遍历所有可能的 Header，找到第一个有效的 IP
        for (String header : Constants.IP_HEADER_CANDIDATES) {
            ip = request.getHeader(header);
            if (!isEmptyIp(ip)) {
                break; // 找到了有效 IP，跳出循环
            }
        }

        // 2. 如果 Header 里都没找到，获取 RemoteAddr
        if (isEmptyIp(ip)) {
            ip = request.getRemoteAddr();
            if (Constants.IP_LOCAL_V4.equals(ip) || Constants.IP_LOCAL_V6.equals(ip)) {
                try {
                    InetAddress inetAddress = InetAddress.getLocalHost();
                    ip = inetAddress.getHostAddress();
                } catch (UnknownHostException e) {
                    log.error("获取本机IP地址失败", e);
                }
            }
        }

        // 3. 处理多级代理的情况
        // 如果经过了多个代理，X-Forwarded-For 的值可能是 "真实IP, 代理1, 代理2..."
        // 我们只取第一个，那个才是真实客户端 IP
        if (ip != null && ip.indexOf(StrUtil.COMMA) > 0) {
            ip = ip.substring(0, ip.indexOf(StrUtil.COMMA));
        }

        return ip;
    }

    /**
     * 判断 IP 是否为空或 unknown
     */
    private static boolean isEmptyIp(String ip) {
        return ip == null || ip.length() == 0 || Constants.IP_UNKNOWN.equalsIgnoreCase(ip);
    }
}
