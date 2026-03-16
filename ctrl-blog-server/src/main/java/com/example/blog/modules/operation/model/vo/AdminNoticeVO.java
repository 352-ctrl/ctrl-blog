package com.example.blog.modules.operation.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "后台公告管理列表对象 (VO)", title = "AdminNoticeVO")
public class AdminNoticeVO {
    @Schema(description = "公告ID", type = "string", example = "1623456789012345678")
    private Long id;

    @Schema(
            description = "原始内容 (Markdown格式, 用于编辑回显)",
            example = "## 系统维护通知\n\n> 请注意备份数据..."
    )
    private String content;

    @Schema(
            description = "渲染内容 (HTML格式, 用于预览)",
            example = "<h2>系统维护通知</h2><blockquote>请注意备份数据...</blockquote>"
    )
    private String contentHtml;

    @Schema(
            description = "状态 (0-隐藏/草稿, 1-显示/发布)",
            example = "1",
            allowableValues = {"0", "1"}
    )
    private Integer status;

    @Schema(
            description = "是否置顶 (0-不置顶, 1-置顶)",
            example = "0",
            allowableValues = {"0", "1"}
    )
    private Integer isTop;

    @Schema(
            description = "创建时间",
            example = "2023-10-24 10:24:00",
            type = "string"
    )
    private LocalDateTime createTime;
}