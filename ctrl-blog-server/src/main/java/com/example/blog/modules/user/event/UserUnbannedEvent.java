package com.example.blog.modules.user.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户被解封事件
 */
@Getter
public class UserUnbannedEvent extends ApplicationEvent {

    private final Long userId;

    public UserUnbannedEvent(Object source, Long userId) {
        super(source);
        this.userId = userId;
    }
}