package com.example.blog.modules.system.model.dto;

import com.example.blog.common.base.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统操作日志查询参数 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "操作日志查询参数")
public class SysOperLogQueryDTO extends PageQueryDTO {

    @Schema(description = "操作模块(模糊匹配，如：文章管理)")
    private String module;

    @Schema(description = "操作人昵称(模糊匹配)")
    private String nickname;

    @Schema(description = "操作状态(1:成功 0:失败)")
    private Integer status;

    @Schema(description = "搜索起始时间(格式：yyyy-MM-dd HH:mm:ss)")
    private String startTime;

    @Schema(description = "搜索结束时间(格式：yyyy-MM-dd HH:mm:ss)")
    private String endTime;

}