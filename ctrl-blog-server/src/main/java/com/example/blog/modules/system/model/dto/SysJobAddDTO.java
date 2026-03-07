package com.example.blog.modules.system.model.dto;

import com.example.blog.common.enums.BizStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "新增定时任务请求参数")
public class SysJobAddDTO {

    @Schema(description = "任务名称", example = "文章浏览量同步", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 64, message = "任务名称不能超过64个字符")
    private String jobName;

    @Schema(description = "任务组名", example = "DEFAULT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "任务组名不能为空")
    @Size(max = 64, message = "任务组名不能超过64个字符")
    @Builder.Default
    private String jobGroup = BizStatus.JobGroup.DEFAULT.getValue();

    @Schema(description = "调用目标字符串", example = "articleTask.syncViewCount()", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "调用目标不能为空")
    @Size(max = 500, message = "调用目标字符串长度不能超过500")
    @Pattern(regexp = "^[A-Za-z0-9_]+\\.[A-Za-z0-9_]+\\(.*?\\)$", message = "调用目标格式错误，示例：beanName.methodName()")
    private String invokeTarget;

    @Schema(description = "Cron执行表达式", example = "0 0/10 * * * ?", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Cron执行表达式不能为空")
    @Size(max = 255, message = "Cron表达式不能超过255个字符")
    private String cronExpression;

    @Schema(description = "计划执行错误策略 (1:立即执行 2:执行一次 3:放弃执行)", example = "1")
    @NotNull(message = "执行策略不能为空")
    @Range(min = 1, max = 3, message = "执行策略非法，仅支持 1(正常) 、2(执行一次)、3(放弃执行)")
    @Builder.Default
    private Integer misfirePolicy = BizStatus.MisfirePolicy.DO_NOTHING.getValue();

    @Schema(description = "状态 (0:正常 1:暂停)", example = "0")
    @NotNull(message = "状态不能为空")
    @Range(min = 0, max = 1, message = "状态值非法，仅支持 0(正常) 或 1(暂停)")
    @Builder.Default
    private Integer status = BizStatus.JobStatus.NORMAL.getValue();

    @Schema(description = "备注信息")
    @Size(max = 500, message = "备注信息不能超过500个字符")
    private String remark;
}
