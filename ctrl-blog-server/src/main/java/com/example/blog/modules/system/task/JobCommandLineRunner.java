package com.example.blog.modules.system.task;

import com.example.blog.modules.system.model.entity.SysJob;
import com.example.blog.modules.system.mapper.SysJobMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 项目启动任务加载器
 * 职责：项目启动后，将数据库中的定时任务加载到 Quartz 调度器中
 */
@Component
@Slf4j
public class JobCommandLineRunner implements CommandLineRunner {

    @Resource
    private SysJobMapper sysJobMapper;

    @Resource
    private ScheduleUtils scheduleUtils;

    @Resource
    private Scheduler scheduler;

    @Override
    public void run(String... args) {
        log.info("正在初始化定时任务...");
        try {
            // 1. 建议先清空调度器，防止热部署或重启时有脏数据
            scheduler.clear();

            // 2. 查询数据库中所有任务
            List<SysJob> jobList = sysJobMapper.selectList(null);

            // 3. 循环创建任务
            int successCount = 0;
            for (SysJob job : jobList) {
                try {
                    // ScheduleUtils.createScheduleJob 内部逻辑会自动判断：
                    // 如果状态是暂停，它会先创建任务，然后立即暂停它，确保任务存在于调度器中
                    scheduleUtils.createScheduleJob(job);
                    successCount++;
                } catch (Exception e) {
                    // 捕获单个任务的异常，防止一个失败导致所有任务都不加载
                    log.error("定时任务加载失败 - 名称: {}, ID: {}", job.getJobName(), job.getId(), e);
                }
            }
            log.info("定时任务初始化完成，共加载 {} 个任务", successCount);

        } catch (SchedulerException e) {
            log.error("定时任务调度器初始化异常", e);
        }
    }
}
