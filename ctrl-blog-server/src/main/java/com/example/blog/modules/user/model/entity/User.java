package com.example.blog.modules.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.base.BaseLogicEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 系统用户实体类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseLogicEntity {

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 个人简介/个性签名
     */
    private String bio;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 角色标识
     */
    private BizStatus.Role role;

    /**
     * 状态: 0-正常, 1-禁用, 2-注销冷静期
     */
    private BizStatus.User status;

    /**
     * 注销申请时间 (用于计算冷静期)
     */
    private LocalDateTime cancelTime;

    /**
     * 封禁到期时间 (为空表示未封禁或已解封)
     * 如果 status 为 DISABLE，且此时间晚于当前时间，则表示在封禁期内
     * 如果此时间为 2099 年或极大值，可代表永久封禁
     */
    private LocalDateTime disableEndTime;

    /**
     * 封禁原因
     */
    private String disableReason;

}