package com.example.blog.modules.operation.model.dto;

import com.example.blog.core.annotation.CheckSensitive;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

/**
 * 更新公告DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新公告请求参数")
public class NoticeUpdateDTO {

    @Schema(description = "公告ID", example = "1623456789012345678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "公告ID不能为空")
    @Positive(message = "公告ID必须为正整数")
    private Long id;

    @Schema(description = "公告内容", example = "系统维护已完成，感谢您的配合。", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "公告内容不能为空")
    @CheckSensitive(message = "公告内容包含违规词汇，请修改")
    private String content;

    @Schema(description = "公告状态 (0:隐藏, 1:显示)", example = "1")
    @NotNull(message = "公告状态不能为空")
    @Range(min = 0, max = 1, message = "公告状态仅支持 0(隐藏) 或 1(显示)")
    private Integer status;

    @Schema(description = "是否置顶 (0:否, 1:是)", example = "0")
    @NotNull(message = "置顶状态不能为空")
    @Range(min = 0, max = 1, message = "置顶状态仅支持 0(否) 或 1(是)")
    private Integer isTop;

}