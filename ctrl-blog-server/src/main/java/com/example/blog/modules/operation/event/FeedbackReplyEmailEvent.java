package com.example.blog.modules.operation.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FeedbackReplyEmailEvent extends ApplicationEvent {
    private final String contactEmail;
    private final String content;
    private final String adminReply;
    private final String statusText;
    private final String replyColor;

    public FeedbackReplyEmailEvent(Object source, String contactEmail, String content,
                                   String adminReply, String statusText, String replyColor) {
        super(source);
        this.contactEmail = contactEmail;
        this.content = content;
        this.adminReply = adminReply;
        this.statusText = statusText;
        this.replyColor = replyColor;
    }
}