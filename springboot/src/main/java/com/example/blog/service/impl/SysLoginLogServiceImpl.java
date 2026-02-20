package com.example.blog.service.impl;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.Constants;
import com.example.blog.convert.SysLoginLogConvert;
import com.example.blog.dto.SysLoginLogQueryDTO;
import com.example.blog.entity.SysLoginLog;
import com.example.blog.mapper.SysLoginLogMapper;
import com.example.blog.service.SysLoginLogService;
import com.example.blog.utils.IpUtils;
import com.example.blog.vo.SysLoginLogVO;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    @Resource
    private SysLoginLogConvert sysLoginLogConvert;

    @Async // 异步执行，不阻塞主线程
    @Override
    public void recordLoginLog(String email, Integer status, String message, String ip, String userAgent) {
        // 解析 UserAgent 获取浏览器和系统信息
        UserAgent ua = UserAgentUtil.parse(userAgent);

        // 使用 IpUtils 获取真实物理地点
        String location = IpUtils.getCityInfo(ip);

        // 组装实体
        SysLoginLog loginLog = SysLoginLog.builder()
                .email(email)
                .ip(ip)
                .location(location)
                .browser(ua != null ? ua.getBrowser().getName() : Constants.UNKNOWN)
                .os(ua != null ? ua.getOs().getName() : Constants.UNKNOWN)
                .status(status)
                .message(message)
                .createTime(LocalDateTime.now())
                .build();

        // 存储
        this.save(loginLog);
    }

    @Override
    public IPage<SysLoginLogVO> pageAdminLoginLog(SysLoginLogQueryDTO queryDTO) {
        LambdaQueryWrapper<SysLoginLog> queryWrapper = new LambdaQueryWrapper<>();

        // 时间范围过滤 (ge: 大于等于, le: 小于等于)
        queryWrapper.ge(StringUtils.hasText(queryDTO.getStartTime()), SysLoginLog::getCreateTime, queryDTO.getStartTime())
                .le(StringUtils.hasText(queryDTO.getEndTime()), SysLoginLog::getCreateTime, queryDTO.getEndTime())
                // 按状态精确匹配
                .eq(queryDTO.getStatus() != null, SysLoginLog::getStatus, queryDTO.getStatus())
                // 按时间倒序，最新的在最前面
                .orderByDesc(SysLoginLog::getCreateTime);

        Page<SysLoginLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<SysLoginLog> entityPage = this.page(page, queryWrapper);
        return entityPage.convert(sysLoginLogConvert::entityToVo);
    }
}