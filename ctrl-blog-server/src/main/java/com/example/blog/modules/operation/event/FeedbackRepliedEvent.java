package com.example.blog.modules.operation.event;

import com.example.blog.common.enums.BizStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 反馈已回复事件
 */
@Getter
public class FeedbackRepliedEvent extends ApplicationEvent {
    private final Long userId;         // 接收通知的用户ID
    private final Long feedbackId;     // 关联的反馈ID
    private final String content;      // 用户的原始反馈
    private final String adminReply;   // 管理员回复
    private final BizStatus.FeedbackStatus status; // 处理状态

    public FeedbackRepliedEvent(Object source, Long userId, Long feedbackId, String content, String adminReply, BizStatus.FeedbackStatus status) {
        super(source);
        this.userId = userId;
        this.feedbackId = feedbackId;
        this.content = content;
        this.adminReply = adminReply;
        this.status = status;
    }
}