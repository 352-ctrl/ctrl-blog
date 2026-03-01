package com.example.blog.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户删除事件
 */
@Getter
@AllArgsConstructor
public class UserDeletedEvent {
    private final Long userId;
}
