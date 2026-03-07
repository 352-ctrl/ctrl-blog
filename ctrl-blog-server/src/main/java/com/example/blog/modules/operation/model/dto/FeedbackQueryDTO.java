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
 * 查询意见反馈DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "意见反馈分页查询条件")
public class FeedbackQueryDTO extends PageQueryDTO {

    @Schema(description = "处理状态 (0:待处理, 1:处理中, 2:已解决, 3:已驳回)", example = "0")
    @Min(0)
    @Max(3)
    private Integer status;

    @Schema(description = "反馈类型 (0:意见建议, 1:BUG反馈, 2:其他)", example = "1")
    @Min(0)
    @Max(3)
    private Integer type;
}