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
@Schema(description = "后台内容举报列表对象 (VO)", title = "AdminReportVO")
public class AdminReportVO {

    @Schema(description = "举报记录ID", type = "string", example = "1")
    private Long id;

    @Schema(description = "举报人ID", type = "string", example = "1001")
    private Long userId;

    @Schema(description = "举报人昵称 (需组装)", example = "热心网友")
    private String userNickname;

    @Schema(description = "举报目标类型 (COMMENT, ARTICLE, USER)", example = "COMMENT")
    private String targetType;

    @Schema(description = "举报目标ID", type = "string", example = "123456")
    private Long targetId;

    @Schema(description = "被举报的目标内容摘要/标题 (需组装，方便管理员直观审核)", example = "点击链接赚大钱...")
    private String targetSummary;

    @Schema(description = "举报原因", example = "垃圾广告")
    private String reason;

    @Schema(description = "详细补充说明", example = "该用户在评论区频繁发布违规广告链接。")
    private String content;

    @Schema(description = "处理状态 (0-待处理, 1-举报属实已处罚, 2-驳回/恶意举报)", example = "0")
    private Integer status;

    @Schema(description = "内部处理备注", example = "已核实并删除评论")
    private String adminNote;

    @Schema(description = "提交时间", example = "2023-10-24 10:24:00", type = "string", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}