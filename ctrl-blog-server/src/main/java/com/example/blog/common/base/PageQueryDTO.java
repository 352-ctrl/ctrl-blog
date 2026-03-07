package com.example.blog.common.base;

import com.example.blog.common.constants.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 分页查询通用DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "基础分页查询参数")
public class PageQueryDTO {

    @Schema(description = "当前页码", example = "1", defaultValue = "1")
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小值为1")
    @Builder.Default
    private Integer pageNum = Constants.PAGE_NUM_DEFAULT;

    @Schema(description = "每页数量 (10-100)", example = "10", defaultValue = "10")
    @NotNull(message = "每页大小不能为空")
    @Min(value = 10, message = "每页大小最小值为10")
    @Max(value = 100, message = "每页大小最大值为100")
    @Builder.Default
    private Integer pageSize = Constants.PAGE_SIZE_DEFAULT;

}