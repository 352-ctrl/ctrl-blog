package com.example.blog.modules.user.event;

import com.example.blog.common.enums.BizStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户安全/状态相关变更事件 (用于发送系统通知)
 */
@Getter
public class UserSecurityEvent extends ApplicationEvent {
    private final Long userId;
    private final BizStatus.SecurityEventType eventType; // 可选值: "PASSWORD_RESET", "ROLE_CHANGE"
    private final BizStatus.Role newRole; // 仅角色变更时有值

    public UserSecurityEvent(Object source, Long userId, BizStatus.SecurityEventType eventType, BizStatus.Role newRole) {
        super(source);
        this.userId = userId;
        this.eventType = eventType;
        this.newRole = newRole;
    }
}