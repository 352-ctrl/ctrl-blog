package com.example.blog.modules.user.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户信息变更事件 (用于触发清理文章缓存)
 */
@Getter
public class UserInfoChangedEvent extends ApplicationEvent {
    private final Long userId;

    public UserInfoChangedEvent(Object source, Long userId) {
        super(source);
        this.userId = userId;
    }
}