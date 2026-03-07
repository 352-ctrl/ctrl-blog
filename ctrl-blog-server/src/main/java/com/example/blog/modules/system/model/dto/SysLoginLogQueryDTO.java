package com.example.blog.modules.system.model.dto;

import com.example.blog.common.base.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "登录日志查询参数")
public class SysLoginLogQueryDTO extends PageQueryDTO {

    @Schema(description = "登录状态(1成功 0失败)")
    private Integer status;

    @Schema(description = "搜索起始时间(格式：yyyy-MM-dd HH:mm:ss)")
    private String startTime;

    @Schema(description = "搜索结束时间(格式：yyyy-MM-dd HH:mm:ss)")
    private String endTime;

}