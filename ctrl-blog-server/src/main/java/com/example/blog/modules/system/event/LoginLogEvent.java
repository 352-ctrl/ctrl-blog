package com.example.blog.modules.system.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LoginLogEvent extends ApplicationEvent {
    private final String email;
    private final Integer status;
    private final String msg;
    private final String ip;
    private final String userAgent;

    public LoginLogEvent(Object source, String email, Integer status, String msg, String ip, String userAgent) {
        super(source);
        this.email = email;
        this.status = status;
        this.msg = msg;
        this.ip = ip;
        this.userAgent = userAgent;
    }
}