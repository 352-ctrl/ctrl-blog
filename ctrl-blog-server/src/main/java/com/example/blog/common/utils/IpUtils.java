package com.example.blog.common.utils;

import cn.hutool.core.util.StrUtil;
import com.example.blog.common.constants.Constants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP地址获取工具类
 */
@Slf4j
public class IpUtils {

    // 预先加载的 xdb 全内存缓存
    private static byte[] cBuff;

    static {
        try {
            // 1、使用常量替换魔法值
            ClassPathResource resource = new ClassPathResource(Constants.IP2REGION_FILE_PATH);
            InputStream is = resource.getInputStream();
            cBuff = FileCopyUtils.copyToByteArray(is);
            log.info("{} 离线IP库加载成功!", Constants.IP2REGION_FILE_PATH);
        } catch (Exception e) {
            log.error("加载 {} 文件失败, 请检查 resources 目录下是否存在该文件", Constants.IP2REGION_FILE_PATH, e);
        }
    }

    /**
     * 根据 IP 获取实际物理位置
     * @param ip IP地址
     * @return 物理位置（例：中国 广东省 深圳市 电信）
     */
    public static String getCityInfo(String ip) {
        if (isEmptyIp(ip)) {
            return Constants.LOCATION_UNKNOWN;
        }

        // 过滤内网 IP (使用常量替换魔法值)
        if (Constants.IP_LOCAL_V4.equals(ip)
                || Constants.IP_LOCAL_V6.equals(ip)
                || ip.startsWith(Constants.IP_LOCAL_PREFIX_192)
                || ip.startsWith(Constants.IP_LOCAL_PREFIX_10)) {
            return Constants.LOCATION_LOCAL_IP;
        }

        try {
            // 使用内存的 cBuff 缓存创建一个完全基于内存的查询对象
            Searcher searcher = Searcher.newWithBuffer(cBuff);

            // 查询，IPv4 和 IPv6 都支持
            String region = searcher.search(ip);

            // 原始的 region 格式通常为：国家|区域|省份|城市|ISP
            if (region != null) {
                return region.replace("|0|", StrUtil.SPACE).replace("|", StrUtil.SPACE).trim();
            }
        } catch (Exception e) {
            log.error("根据 IP [{}] 获取地理位置失败: {}", ip, e.getMessage());
        }
        return Constants.LOCATION_UNKNOWN;
    }

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
        if (ip != null && ip.indexOf(StrUtil.COMMA) > 0) {
            ip = ip.substring(0, ip.indexOf(StrUtil.COMMA));
        }

        return ip;
    }

    /**
     * 判断 IP 是否为空或 unknown
     */
    private static boolean isEmptyIp(String ip) {
        return ip == null || ip.isEmpty() || Constants.IP_UNKNOWN.equalsIgnoreCase(ip);
    }
}