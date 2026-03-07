package com.example.blog.modules.system.model.dto;

import com.example.blog.common.enums.BizStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "修改任务状态请求参数")
public class ChangeStatusDto {

    @Schema(description = "状态 (0:正常 1:暂停)", example = "0")
    @NotNull(message = "状态不能为空")
    @Range(min = 0, max = 1, message = "状态值非法，仅支持 0(正常) 或 1(暂停)")
    @Builder.Default
    private Integer status = BizStatus.JobStatus.NORMAL.getValue();

}
