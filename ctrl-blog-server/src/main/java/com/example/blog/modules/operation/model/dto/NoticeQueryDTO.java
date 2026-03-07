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
 * 查询公告DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "公告分页查询条件")
public class NoticeQueryDTO extends PageQueryDTO {

    @Schema(description = "公告内容 (模糊查询)", example = "维护")
    private String content;

    @Schema(description = "公告状态 (0:隐藏, 1:显示)", example = "1")
    @Min(0)
    @Max(1)
    private Integer status;

    @Schema(description = "是否置顶 (0:否, 1:是)", example = "0")
    @Min(0)
    @Max(1)
    private Integer isTop;

}