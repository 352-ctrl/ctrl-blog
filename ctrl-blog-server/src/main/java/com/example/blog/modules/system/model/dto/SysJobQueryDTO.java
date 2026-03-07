package com.example.blog.modules.system.model.dto;

import com.example.blog.common.base.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "定时任务查询条件")
public class SysJobQueryDTO extends PageQueryDTO {

    @Schema(description = "任务名称 (模糊查询)", example = "文章")
    private String jobName;

    @Schema(description = "任务组名", example = "DEFAULT")
    private String jobGroup;

    @Schema(description = "任务状态 (0:正常 1:暂停)", example = "0")
    private Integer status;
}
