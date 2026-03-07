package com.example.blog.modules.operation.model.dto;

import com.example.blog.common.enums.BizStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统内部消息发送 DTO (无需对外暴露，用于 Service 层解耦)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSendDTO {
    /** 接收方用户ID */
    private Long toUserId;
    /** 发送方用户ID (系统通知可为NULL) */
    private Long fromUserId;
    /** 消息大类：SYSTEM, LIKE, COMMENT */
    private BizStatus.MessageType type;
    /** 消息标题 (系统通知必填，互动消息可自动生成) */
    private String title;
    /** 消息主体内容 (如评论的具体文字) */
    private String content;
    /** 关联业务路由ID (例如：文章ID，用于决定跳转到哪个页面) */
    private Long bizId;
    /** 业务类型 (ARTICLE, COMMENT, FEEDBACK) */
    private BizStatus.MessageBizType bizType;
    /** 锚点目标ID (例如：具体的评论ID，用于页面内精准滚动高亮) */
    private Long targetId;
}