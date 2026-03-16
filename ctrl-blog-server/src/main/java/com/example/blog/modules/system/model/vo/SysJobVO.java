package com.example.blog.modules.system.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "定时任务详情")
public class SysJobVO {

    @Schema(description = "任务ID", type = "string", example = "1623456789012345678")
    private Long id;

    @Schema(description = "任务名称")
    private String jobName;

    @Schema(description = "任务组名")
    private String jobGroup;

    @Schema(description = "调用目标字符串")
    private String invokeTarget;

    @Schema(description = "Cron执行表达式")
    private String cronExpression;

    @Schema(description = "计划执行错误策略 (1:立即执行 2:执行一次 3:放弃执行)")
    private Integer misfirePolicy;

    @Schema(description = "状态 (0:正常 1:暂停)")
    private Integer status;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}