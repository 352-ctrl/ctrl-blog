package com.example.blog.modules.operation.model.vo;

import com.example.blog.common.enums.BizStatus;
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
@Schema(description = "系统消息展示层对象 (VO)", title = "MessageVO")
public class MessageVO {

    @Schema(description = "消息ID", type = "string", example = "1623456789012345678")
    private Long id;

    @Schema(description = "发送方用户ID (系统通知为空)", type = "string", example = "10086")
    private Long fromUserId;

    @Schema(description = "发送方昵称 (需要连表查询组装)", example = "张三")
    private String fromUserNickname;

    @Schema(description = "发送方头像 (需要连表查询组装)", example = "https://xxx.png")
    private String fromUserAvatar;

    @Schema(description = "消息大类", example = "LIKE")
    private BizStatus.MessageType type;

    @Schema(description = "消息标题", example = "收到了新的点赞")
    private String title;

    @Schema(description = "消息主体内容摘要", example = "写得太好了，受益匪浅！")
    private String content;

    @Schema(description = "关联业务ID", type = "string", example = "112233")
    private Long bizId;

    @Schema(description = "业务类型", example = "ARTICLE")
    private BizStatus.MessageBizType bizType;

    @Schema(description = "锚点目标ID (例如：具体的评论ID，用于页面内精准滚动高亮)", type = "string", example = "445566")
    private Long targetId;

    @Schema(
            description = "阅读状态 (0:未读, 1:已读)",
            type = "integer",
            allowableValues = {"0", "1"},
            example = "0"
    )
    private BizStatus.ReadStatus isRead;

    @Schema(
            description = "创建时间/通知时间",
            example = "2023-10-24 10:24:00",
            type = "string"
    )
    private LocalDateTime createTime;
}