package com.example.blog.event;

import com.example.blog.dto.user.UserUpdateDTO;
import com.example.blog.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户信息变更事件
 */
@Getter
@AllArgsConstructor
public class UserChangedEvent {
    private final User user;
    private final UserUpdateDTO updateDTO; // 可能包含修改前后的差异信息
    private final boolean roleChanged;
}