package com.example.blog.modules.operation.model.dto;

import com.example.blog.core.annotation.CheckSensitive;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

/**
 * 提交意见反馈DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "新增意见反馈请求参数")
public class FeedbackAddDTO {

    @Schema(description = "反馈类型 (0:意见建议, 1:BUG反馈, 2:其他)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "反馈类型不能为空")
    @Range(min = 0, max = 3, message = "非法的反馈类型")
    private Integer type;

    @Schema(description = "反馈详细内容", example = "页面在手机端显示错位", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 1000, message = "内容长度不能超过1000个字符")
    @CheckSensitive(message = "反馈内容包含违规词汇，请修改")
    private String content;

    @Schema(description = "附加截图 (JSON数组或逗号分隔的URL)", example = "[\"https://img.example.com/1.png\"]")
    @Size(max = 1000, message = "图片链接总长度过长")
    private String images;

    @Schema(description = "联系邮箱", example = "newuser@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 100, message = "联系邮箱长度不能超过100个字符")
    @Email(message = "邮箱格式不正确")
    private String contactEmail;

}