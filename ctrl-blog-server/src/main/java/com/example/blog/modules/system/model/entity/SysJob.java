package com.example.blog.modules.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.base.BaseUpdateEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 定时任务实体类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_job")
public class SysJob extends BaseUpdateEntity {

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 任务组名
     */
    private BizStatus.JobGroup jobGroup;

    /**
     * 调用目标字符串
     * 示例：bean名称.方法名(参数)
     * 例如：articleSyncTask.syncViewCount()
     */
    private String invokeTarget;

    /**
     * cron执行表达式
     */
    private String cronExpression;

    /**
     * 计划执行错误策略 (1:立即执行 2:执行一次 3:放弃执行)
     */
    private Integer misfirePolicy;

    /**
     * 状态 (0:正常 1:暂停)
     */
    private BizStatus.JobStatus status;

    /**
     * 备注信息
     */
    private String remark;
}
