package com.example.blog.modules.operation.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

/**
 * 后台处理举报DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "后台处理举报请求参数")
public class ReportProcessDTO {

    @Schema(description = "举报记录ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "举报记录ID不能为空")
    @Positive(message = "举报记录ID必须为正整数")
    private Long id;

    @Schema(description = "处理状态 (1:举报属实已处罚, 2:驳回/恶意举报)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "处理状态不能为空")
    @Range(min = 1, max = 2, message = "非法的处理状态")
    private Integer status;

    @Schema(description = "内部处理备注", example = "已核实，封禁该用户账号。")
    @Size(max = 500, message = "备注不能超过500个字符")
    private String adminNote;

    @Schema(description = "封禁天数（仅当举报属实且目标为用户时有效。-1永久，0不封禁，>0封禁天数）", example = "7")
    @Min(value = -1, message = "封禁天数不能小于-1")
    private Integer disableDays;
}