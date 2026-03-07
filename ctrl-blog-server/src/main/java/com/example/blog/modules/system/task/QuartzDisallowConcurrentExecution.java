package com.example.blog.modules.system.task;

import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.system.model.entity.SysJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

/**
 * 定时任务处理 (禁止并发执行)
 */
@DisallowConcurrentExecution
@Slf4j
public class QuartzDisallowConcurrentExecution implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        // 从参数中获取 SysJob 对象
        SysJob sysJob = (SysJob) context.getMergedJobDataMap().get(BizStatus.JOB_PROPERTIES);
        if (sysJob == null) {
            log.warn("任务执行终止：未找到任务属性配置");
            return;
        }
        long startTime = System.currentTimeMillis();
        try {
            log.info("任务开始执行 - ID: {}, 名称: {}, 目标: {}", sysJob.getId(), sysJob.getJobName(), sysJob.getInvokeTarget());

            // 执行反射调用
            JobInvokeUtil.invokeMethod(sysJob);

            long duration = System.currentTimeMillis() - startTime;
            log.info("任务执行成功 - ID: {}, 耗时: {}ms", sysJob.getId(), duration);

            // 扩展点：可以在这里异步记录任务执行日志到数据库 (sys_job_log)
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("任务执行异常 - ID: {}, 耗时: {}ms, 错误: {}", sysJob.getId(), duration, e.getMessage());
            // 扩展点：记录失败详情到数据库，并发送告警邮件/钉钉通知
        }
    }
}
