package com.example.blog.modules.system.service.impl;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.Constants;
import com.example.blog.modules.system.model.convert.SysLoginLogConvert;
import com.example.blog.modules.system.model.dto.SysLoginLogQueryDTO;
import com.example.blog.modules.system.model.entity.SysLoginLog;
import com.example.blog.modules.system.event.LoginLogEvent;
import com.example.blog.modules.system.mapper.SysLoginLogMapper;
import com.example.blog.modules.system.service.SysLoginLogService;
import com.example.blog.common.utils.IpUtils;
import com.example.blog.modules.system.model.vo.SysLoginLogVO;
import jakarta.annotation.Resource;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    @Resource
    private SysLoginLogConvert sysLoginLogConvert;

    @EventListener
    @Async
    public void onLoginLogEvent(LoginLogEvent event) {
        this.recordLoginLog(event.getEmail(), event.getStatus(), event.getMsg(), event.getIp(), event.getUserAgent());
    }

    @Async // 异步执行，不阻塞主线程
    @Override
    public void recordLoginLog(String email, Integer status, String message, String ip, String userAgent) {
        Assert.hasText(email, "登录邮箱不能为空");
        Assert.notNull(status, "登录状态不能为空");
        Assert.hasText(ip, "IP地址不能为空");

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
        Assert.notNull(queryDTO, "分页查询参数不能为空");

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

    @Override
    public int clearLoginLogTrash(LocalDateTime logLimitDate) {
        if (logLimitDate == null) {
            return 0;
        }
        // 直接调用底层 Mapper 进行物理删除
        return this.baseMapper.physicalDeleteExpiredLogs(logLimitDate);
    }

}