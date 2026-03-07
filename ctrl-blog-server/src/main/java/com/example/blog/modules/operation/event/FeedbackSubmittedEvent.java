package com.example.blog.modules.operation.event;

import org.springframework.context.ApplicationEvent;
import lombok.Getter;

@Getter
public class FeedbackSubmittedEvent extends ApplicationEvent {
    private final Long userId;

    public FeedbackSubmittedEvent(Object source, Long userId) {
        super(source);
        this.userId = userId;
    }
}