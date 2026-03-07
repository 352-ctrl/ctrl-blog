package com.example.blog.modules.user.event;

import org.springframework.context.ApplicationEvent;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class UserBannedEvent extends ApplicationEvent {
    private final Long userId;
    private final String email;
    private final String nickname;
    private final String banReason;
    private final LocalDateTime disableEndTime;

    public UserBannedEvent(Object source, Long userId, String email, String nickname, String banReason, LocalDateTime disableEndTime) {
        super(source);
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.banReason = banReason;
        this.disableEndTime = disableEndTime;
    }
}