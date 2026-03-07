package com.example.blog.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.modules.system.model.convert.SysOperLogConvert;
import com.example.blog.modules.system.model.dto.SysOperLogQueryDTO;
import com.example.blog.modules.system.model.entity.SysOperLog;
import com.example.blog.modules.system.mapper.SysOperLogMapper;
import com.example.blog.modules.system.service.SysOperLogService;
import com.example.blog.modules.system.model.vo.SysOperLogVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 系统日志业务实现类
 * 实现保存系统日志的业务逻辑
 */
@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService {

    @Resource
    private SysOperLogConvert sysOperLogConvert;

    @Override
    public void addLog(SysOperLog sysOperLog) {
        Assert.notNull(sysOperLog, "操作日志对象不能为空");

        this.save(sysOperLog);
    }

    @Override
    public IPage<SysOperLogVO> pageAdminOperLog(SysOperLogQueryDTO queryDTO) {
        Assert.notNull(queryDTO, "分页查询参数不能为空");

        LambdaQueryWrapper<SysOperLog> queryWrapper = new LambdaQueryWrapper<>();

        // 1. 模糊匹配：模块名称和操作人昵称
        queryWrapper.like(StringUtils.hasText(queryDTO.getModule()), SysOperLog::getModule, queryDTO.getModule())
                .like(StringUtils.hasText(queryDTO.getNickname()), SysOperLog::getNickname, queryDTO.getNickname());

        // 2. 精确匹配：状态
        queryWrapper.eq(queryDTO.getStatus() != null, SysOperLog::getStatus, queryDTO.getStatus());

        // 3. 时间范围过滤 (ge: 大于等于, le: 小于等于)
        queryWrapper.ge(StringUtils.hasText(queryDTO.getStartTime()), SysOperLog::getCreateTime, queryDTO.getStartTime())
                .le(StringUtils.hasText(queryDTO.getEndTime()), SysOperLog::getCreateTime, queryDTO.getEndTime());

        // 4. 排序：按创建时间倒序
        queryWrapper.orderByDesc(SysOperLog::getCreateTime);

        // 5. 执行分页查询
        Page<SysOperLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<SysOperLog> entityPage = this.page(page, queryWrapper);
        return entityPage.convert(sysOperLogConvert::entityToVo);
    }

    @Override
    public int clearOperLogTrash(LocalDateTime logLimitDate) {
        return this.baseMapper.physicalDeleteExpiredLogs(logLimitDate);
    }

}