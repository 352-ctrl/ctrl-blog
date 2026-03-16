package com.example.blog.modules.operation.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FeedbackAdminNoticeEvent extends ApplicationEvent {
    private final String content;
    private final String contactEmail;

    public FeedbackAdminNoticeEvent(Object source, String content, String contactEmail) {
        super(source);
        this.content = content;
        this.contactEmail = contactEmail;
    }
}