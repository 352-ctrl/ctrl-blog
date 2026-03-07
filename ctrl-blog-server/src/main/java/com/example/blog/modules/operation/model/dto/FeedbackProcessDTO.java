package com.example.blog.modules.operation.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

/**
 * 后台处理反馈DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "后台处理反馈请求参数")
public class FeedbackProcessDTO {

    @Schema(description = "反馈记录ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "反馈ID不能为空")
    @Positive(message = "反馈ID必须为正整数")
    private Long id;

    @Schema(description = "处理状态 (1:处理中, 2:已解决, 3:已驳回)", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "处理状态不能为空")
    @Range(min = 1, max = 3, message = "非法的处理状态")
    private Integer status;

    @Schema(description = "管理员回复内容", example = "已修复该BUG，感谢您的反馈。")
    @Size(max = 500, message = "回复内容不能超过500个字符")
    private String adminReply;
}