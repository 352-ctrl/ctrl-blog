package com.example.blog.modules.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.core.exception.CustomerException;
import com.example.blog.modules.system.mapper.SysJobMapper;
import com.example.blog.modules.system.model.convert.SysJobConvert;
import com.example.blog.modules.system.model.dto.SysJobAddDTO;
import com.example.blog.modules.system.model.dto.SysJobQueryDTO;
import com.example.blog.modules.system.model.dto.SysJobUpdateDTO;
import com.example.blog.modules.system.model.entity.SysJob;
import com.example.blog.modules.system.model.vo.SysJobVO;
import com.example.blog.modules.system.service.SysJobService;
import com.example.blog.modules.system.task.ScheduleUtils;
import jakarta.annotation.Resource;
import org.quartz.CronExpression;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

/**
 * 定时任务调度 Service 实现类
 */
@Service
public class SysJobServiceImpl extends ServiceImpl<SysJobMapper, SysJob> implements SysJobService {

    @Resource
    private ScheduleUtils scheduleUtils;

    @Resource
    private SysJobConvert sysJobConvert;

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 校验 invokeTarget (例如 "testTask.sync()") 在 Spring 容器中是否真实存在
     */
    private void validateInvokeTarget(String invokeTarget) {
        if (StrUtil.isBlank(invokeTarget)) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_INVOKE_TARGET_EMPTY);
        }

        int dotIndex = invokeTarget.indexOf(StrUtil.DOT);
        if (dotIndex == -1) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_INVOKE_TARGET_FORMAT_ERROR);
        }

        String beanName = invokeTarget.substring(0, dotIndex);

        // 防御点：去 Spring IoC 容器里查有没有这个 Bean
        if (!applicationContext.containsBean(beanName)) {
            throw new CustomerException(ResultCode.PARAM_ERROR, String.format(MessageConstants.MSG_INVOKE_TARGET_BEAN_NOT_FOUND, beanName));
        }
    }

    /**
     * 辅助方法：判断是否需要更新 Quartz 调度器
     */
    private boolean isScheduleChanged(SysJob newJob, String oldCron, String oldTarget, BizStatus.JobGroup oldGroup, BizStatus.JobStatus oldStatus) {
        return !Objects.equals(newJob.getCronExpression(), oldCron)
                || !Objects.equals(newJob.getInvokeTarget(), oldTarget)
                || !Objects.equals(newJob.getJobGroup(), oldGroup)
                || !Objects.equals(newJob.getStatus(), oldStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addJob(SysJobAddDTO addDTO) {
        Assert.notNull(addDTO, "新增任务参数不能为空");

        // 校验 Cron 表达式
        if (!CronExpression.isValidExpression(addDTO.getCronExpression())) {
            throw new CustomerException(MessageConstants.MSG_CRON_FORMAT_ERROR);
        }

        validateInvokeTarget(addDTO.getInvokeTarget());

        SysJob sysJob = sysJobConvert.addDtoToEntity(addDTO);

        // 插入数据库
        boolean success = this.save(sysJob);

        // 如果插入成功，且状态为正常，则在 Quartz 中创建任务
        if (success) {
            scheduleUtils.createScheduleJob(sysJob);
        } else {
            throw new CustomerException(MessageConstants.MSG_SAVE_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateJob(SysJobUpdateDTO  updateDTO) {
        Assert.notNull(updateDTO, "更新任务参数不能为空");
        Assert.notNull(updateDTO.getId(), "任务ID不能为空");

        if (!CronExpression.isValidExpression(updateDTO.getCronExpression())) {
            throw new CustomerException(MessageConstants.MSG_CRON_FORMAT_ERROR);
        }

        validateInvokeTarget(updateDTO.getInvokeTarget());

        SysJob job = this.getById(updateDTO.getId());
        if (job == null) {
            throw new CustomerException(MessageConstants.MSG_JOB_NOT_EXIST);
        }

        // 记录关键字段的旧值，用于后续判断是否需要更新调度器
        String oldCron = job.getCronExpression();
        String oldInvokeTarget = job.getInvokeTarget();
        BizStatus.JobGroup oldJobGroup = job.getJobGroup();
        BizStatus.JobStatus oldStatus = job.getStatus();

        sysJobConvert.updateEntityFromDto(updateDTO, job);
        boolean success = this.updateById(job);
        // 更新 Quartz 调度
        if (!success) {
            throw new CustomerException(MessageConstants.MSG_UPDATE_FAILED);
        }

        // 只有关键调度参数变化时，才操作 Quartz
        // 如果 Cron、调用目标、任务组发生变化，或者状态发生变化
        if (isScheduleChanged(job, oldCron, oldInvokeTarget, oldJobGroup, oldStatus)) {
            scheduleUtils.updateScheduleJob(job);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJobById(Long id) {
        Assert.notNull(id, "任务ID不能为空");

        SysJob job = this.getById(id);
        if (job != null) {
            // 删除 Quartz 中的任务
            scheduleUtils.deleteScheduleJob(job);
            // 删除数据库记录
            this.removeById(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteJobs(List<Long> ids) {
        Assert.notEmpty(ids, "任务ID列表不能为空");

        if (CollUtil.isEmpty(ids)) {
            return;
        }

        List<SysJob> jobs = this.listByIds(ids);

        // 处理 Quartz 调度
        for (SysJob job : jobs) {
            // 删除 Quartz 中的任务（需要 jobGroup 和 jobName）
            scheduleUtils.deleteScheduleJob(job);
        }

        // 删除数据库中的数据
        this.removeBatchByIds(ids);
    }

    @Override
    public void run(Long id) {
        Assert.notNull(id, "任务ID不能为空");

        SysJob job = this.getById(id);
        if (job == null) {
            throw new CustomerException(MessageConstants.MSG_JOB_NOT_EXIST);
        }
        // 立即执行一次
        scheduleUtils.run(job);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        Assert.notNull(id, "任务ID不能为空");
        Assert.notNull(status, "目标状态不能为空");

        SysJob job = this.getById(id);
        if (job == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_JOB_NOT_EXIST);
        }

        BizStatus.JobStatus targetStatus = BizStatus.JobStatus.getByCode(status);

        if (targetStatus == null) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_PARAM_ERROR);
        }

        if (job.getStatus() == targetStatus) {
            return;
        }

        job.setStatus(targetStatus);
        this.updateById(job);

        // 更新 Quartz 状态
        if (targetStatus == BizStatus.JobStatus.NORMAL) {
            scheduleUtils.resumeJob(job);
        } else if (targetStatus == BizStatus.JobStatus.PAUSE) {
            scheduleUtils.pauseJob(job);
        }
    }

    @Override
    public SysJobVO getJobById(Long id) {
        Assert.notNull(id, "任务ID不能为空");

        SysJob job = this.getById(id);
        if (job == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_JOB_NOT_EXIST);
        }
        return sysJobConvert.entityToVo(job);
    }

    @Override
    public IPage<SysJobVO> pageAdminJobs(SysJobQueryDTO queryDTO) {
        Assert.notNull(queryDTO, "分页查询参数不能为空");

        Page<SysJob> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<SysJob> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(queryDTO.getJobName()), SysJob::getJobName, queryDTO.getJobName())
                .eq(StrUtil.isNotBlank(queryDTO.getJobGroup()), SysJob::getJobGroup, queryDTO.getJobGroup())
                .eq(queryDTO.getStatus() != null, SysJob::getStatus, queryDTO.getStatus())
                .orderByDesc(SysJob::getCreateTime);
        IPage<SysJob> jobPage = this.page(page, queryWrapper);
        return jobPage.convert(job -> sysJobConvert.entityToVo(job));
    }
}