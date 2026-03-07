package com.example.blog.modules.system.task;

import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.system.model.entity.SysJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

/**
 * 定时任务工具类
 * 封装 Quartz 的核心 API 操作
 */
@Component
@Slf4j
public class ScheduleUtils {

    @Resource
    private Scheduler scheduler;

    /**
     * 获取 JobKey
     */
    public static JobKey getJobKey(Long jobId, String jobGroup) {
        return JobKey.jobKey(BizStatus.JOB_NAME_PREFIX + jobId, jobGroup);
    }

    /**
     * 获取 TriggerKey
     */
    public static TriggerKey getTriggerKey(Long jobId, String jobGroup) {
        return TriggerKey.triggerKey(BizStatus.JOB_NAME_PREFIX + jobId, jobGroup);
    }

    /**
     * 创建定时任务
     */
    public void createScheduleJob(SysJob job) {
        try {
            JobKey jobKey = getJobKey(job.getId(), job.getJobGroup().getValue());
            TriggerKey triggerKey = getTriggerKey(job.getId(), job.getJobGroup().getValue());

            // 强制清理已存在的同名任务，确保幂等性
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }

            // 构建 JobDetail
            JobDetail jobDetail = JobBuilder.newJob(QuartzDisallowConcurrentExecution.class)
                    .withIdentity(jobKey)
                    .build();

            // 放入参数，运行时通过这些参数执行目标方法
            jobDetail.getJobDataMap().put(BizStatus.JOB_PROPERTIES, job);

            // 构建 Trigger (Cron 触发器)
            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
            // 策略：错过了就放弃执行，等待下一次
            cronScheduleBuilder = handleMisfirePolicy(job, cronScheduleBuilder);

            // 构建 Trigger
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(cronScheduleBuilder)
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);

            // 状态校验：如果是暂停状态，则立即挂起
            if (job.getStatus() == BizStatus.JobStatus.PAUSE) {
                scheduler.pauseJob(jobKey);
            }


            log.info("定时任务创建成功 - ID: {}, 名称: {}", job.getId(), job.getJobName());
        } catch (SchedulerException e) {
            log.error("创建定时任务失败，任务ID: {}", job.getId(), e);
            throw new RuntimeException("创建定时任务失败");
        }
    }

    /**
     * 处理错过触发策略 (Misfire)
     */
    private CronScheduleBuilder handleMisfirePolicy(SysJob job, CronScheduleBuilder cb) {
        Integer misfirePolicy = job.getMisfirePolicy();

        // 1: 立即执行
        if (BizStatus.MisfirePolicy.IGNORE.getValue().equals(misfirePolicy)) {
            return cb.withMisfireHandlingInstructionIgnoreMisfires();
        }
        // 2: 执行一次
        else if (BizStatus.MisfirePolicy.FIRE_ONCE.getValue().equals(misfirePolicy)) {
            return cb.withMisfireHandlingInstructionFireAndProceed();
        }
        // 3: 放弃执行 (默认)
        else if (BizStatus.MisfirePolicy.DO_NOTHING.getValue().equals(misfirePolicy)) {
            return cb.withMisfireHandlingInstructionDoNothing();
        }

        return cb.withMisfireHandlingInstructionDoNothing();
    }

    /**
     * 更新定时任务 (通常是更新 Cron 表达式)
     */
    public void updateScheduleJob(SysJob job) {
        // 删除重建
        deleteScheduleJob(job);
        createScheduleJob(job);
    }

    /**
     * 立即执行任务 (一次性)
     */
    public void run(SysJob job) {
        try {
            // 设置参数
            JobDataMap dataMap = new JobDataMap();
            dataMap.put(BizStatus.JOB_PROPERTIES, job);

            // 触发任务，不影响原有的 Cron 计划
            scheduler.triggerJob(getJobKey(job.getId(), job.getJobGroup().getValue()), dataMap);
        } catch (SchedulerException e) {
            log.error("立即执行定时任务失败", e);
            throw new RuntimeException("立即执行定时任务失败");
        }
    }

    /**
     * 暂停任务
     */
    public void pauseJob(SysJob job) {
        try {
            scheduler.pauseJob(getJobKey(job.getId(), job.getJobGroup().getValue()));
        } catch (SchedulerException e) {
            log.error("暂停定时任务失败", e);
            throw new RuntimeException("暂停定时任务失败");
        }
    }

    /**
     * 恢复任务
     */
    public void resumeJob(SysJob job) {
        try {
            scheduler.resumeJob(getJobKey(job.getId(), job.getJobGroup().getValue()));
        } catch (SchedulerException e) {
            log.error("恢复定时任务失败", e);
            throw new RuntimeException("恢复定时任务失败");
        }
    }

    /**
     * 删除任务
     */
    public void deleteScheduleJob(SysJob job) {
        try {
            JobKey jobKey = getJobKey(job.getId(), job.getJobGroup().getValue());
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            log.error("删除定时任务失败", e);
            throw new RuntimeException("删除定时任务失败");
        }
    }
}