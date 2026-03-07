package com.example.blog.modules.operation.model.dto;

import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.base.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 前台查询评论DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "前台评论分页查询条件")
public class CommentQueryDTO extends PageQueryDTO {

    @Schema(description = "文章ID", example = "1623456789012345678", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long articleId;

    @Schema(description = "排序方式：1-按时间最新(默认)，2-按热度(点赞数)", example = "1")
    private Integer sortType = BizStatus.CommentSort.LATEST.getValue();

}