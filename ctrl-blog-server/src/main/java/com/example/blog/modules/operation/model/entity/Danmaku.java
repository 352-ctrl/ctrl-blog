package com.example.blog.modules.operation.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.blog.common.base.BaseLogicEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 弹幕实体类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("blog_danmaku")
public class Danmaku extends BaseLogicEntity {

    /**
     * 弹幕内容
     */
    private String content;

    /**
     * 发送用户ID (游客为null)
     */
    private Long userId;

    /**
     * 发送者昵称
     */
    private String nickname;

    /**
     * 发送者头像
     */
    private String avatar;
}