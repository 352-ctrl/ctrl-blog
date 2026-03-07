package com.example.blog.modules.operation.event;

import com.example.blog.common.enums.BizStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 互动消息事件（用于解耦点赞、评论等触发的消息发送）
 */
@Getter
public class InteractiveMessageEvent extends ApplicationEvent {
    private final Long toUserId;       // 接收方
    private final Long fromUserId;     // 发送方
    private final BizStatus.MessageType type;           // 消息类型 (点赞/评论)
    private final Long bizId;          // 关联的顶级业务ID (如文章ID)
    private final BizStatus.MessageBizType bizType;     // 关联的业务类型 (文章/评论)
    private final Long targetId;       // 精准定位的ID (如具体的评论ID)
    private final String content;      // 附加内容摘要

    public InteractiveMessageEvent(Object source, Long toUserId, Long fromUserId,
                                   BizStatus.MessageType type, Long bizId,
                                   BizStatus.MessageBizType bizType, Long targetId, String content) {
        super(source);
        this.toUserId = toUserId;
        this.fromUserId = fromUserId;
        this.type = type;
        this.bizId = bizId;
        this.bizType = bizType;
        this.targetId = targetId;
        this.content = content;
    }
}