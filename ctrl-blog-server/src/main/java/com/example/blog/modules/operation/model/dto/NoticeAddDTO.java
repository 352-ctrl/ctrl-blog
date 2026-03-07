package com.example.blog.modules.operation.model.dto;

import com.example.blog.core.annotation.CheckSensitive;
import com.example.blog.common.enums.BizStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

/**
 * 新增公告DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "新增公告请求参数")
public class NoticeAddDTO {

    @Schema(description = "公告内容", example = "系统将于今晚 24:00 进行停机维护，请提前保存数据。", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "公告内容不能为空")
    @CheckSensitive(message = "公告内容包含违规词汇，请修改")
    private String content;

    @Schema(description = "公告状态 (0:隐藏, 1:显示)", example = "1", defaultValue = "1")
    @NotNull(message = "公告状态不能为空")
    @Range(min = 0, max = 1, message = "公告状态仅支持 0(隐藏) 或 1(显示)")
    @Builder.Default
    private Integer status = BizStatus.Notice.SHOW.getValue();

    @Schema(description = "是否置顶 (0:否, 1:是)", example = "0", defaultValue = "0")
    @NotNull(message = "置顶状态不能为空")
    @Range(min = 0, max = 1, message = "置顶状态仅支持 0(否) 或 1(是)")
    @Builder.Default
    private Integer isTop = BizStatus.Common.DISABLE.getValue();

}