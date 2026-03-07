package com.example.blog.modules.operation.model.dto;

import com.example.blog.common.base.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 查询举报记录DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "内容举报分页查询条件")
public class ReportQueryDTO extends PageQueryDTO {

    @Schema(description = "处理状态 (0:待处理, 1:举报属实已处罚, 2:驳回/恶意举报)", example = "0")
    @Min(0)
    @Max(2)
    private Integer status;

    @Schema(description = "举报目标类型 (COMMENT:评论, ARTICLE:文章, USER:用户)", example = "COMMENT")
    private String targetType;
}