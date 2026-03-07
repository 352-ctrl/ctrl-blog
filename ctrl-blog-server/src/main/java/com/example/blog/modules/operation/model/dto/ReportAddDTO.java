package com.example.blog.modules.operation.model.dto;

import com.example.blog.core.annotation.CheckSensitive;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提交内容举报DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "新增内容举报请求参数")
public class ReportAddDTO {

    @Schema(description = "举报目标类型 (COMMENT:评论, ARTICLE:文章, USER:用户)", example = "COMMENT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "举报目标类型不能为空")
    private String targetType;

    @Schema(description = "举报目标ID (如评论的ID)", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "举报目标ID不能为空")
    @Positive(message = "目标ID必须为正整数")
    private Long targetId;

    @Schema(description = "举报原因", example = "垃圾广告", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "举报原因不能为空")
    @Size(max = 100, message = "举报原因过长")
    private String reason;

    @Schema(description = "详细补充说明", example = "该用户在评论区频繁发布违规广告链接。")
    @Size(max = 500, message = "补充说明不能超过500个字符")
    @CheckSensitive(message = "举报内容包含违规词汇，请修改")
    private String content;

}