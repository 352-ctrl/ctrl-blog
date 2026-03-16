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
@Schema(description = "后台意见反馈列表对象 (VO)", title = "AdminFeedbackVO")
public class AdminFeedbackVO {

    @Schema(description = "反馈ID", type = "string", example = "1")
    private Long id;

    @Schema(description = "反馈用户ID (空表示游客)", type = "string", example = "1001")
    private Long userId;

    @Schema(description = "反馈用户昵称 (需要连表或业务层组装)", example = "张三")
    private String userNickname;

    @Schema(description = "反馈类型 (0-意见建议, 1-BUG反馈, 2-其他)", example = "1")
    private Integer type;

    @Schema(description = "反馈详细内容", example = "页面在手机端显示错位")
    private String content;

    @Schema(description = "附加截图", example = "[\"https://img.example.com/1.png\"]")
    private String images;

    @Schema(description = "联系邮箱", example = "user@example.com")
    private String contactEmail;

    @Schema(description = "处理状态 (0-待处理, 1-处理中, 2-已解决, 3-已驳回)", example = "0")
    private Integer status;

    @Schema(description = "管理员回复内容", example = "感谢反馈，已修复。")
    private String adminReply;

    @Schema(description = "提交时间", example = "2023-10-24 10:24:00", type = "string", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}