package com.example.blog.modules.operation.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.base.BaseUpdateEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 系统消息实体类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_message")
public class Message extends BaseUpdateEntity {

    /**
     * 接收方用户ID (谁收到了这条消息)
     */
    private Long toUserId;

    /**
     * 发送方用户ID (谁触发的动作，系统通知可为NULL)
     */
    private Long fromUserId;

    /**
     * 消息大类：SYSTEM-系统通知, LIKE-点赞, COMMENT-评论/回复
     */
    private BizStatus.MessageType type;

    /**
     * 消息标题 (例如："系统升级通知" 或 "新评论提醒")
     */
    private String title;

    /**
     * 消息主体内容 (例如评论的具体文字，或反馈的处理结果)
     */
    private String content;

    /**
     * 关联业务ID (例如：文章ID、评论ID、反馈单ID)
     */
    private Long bizId;

    /**
     * 业务类型 (ARTICLE, COMMENT, FEEDBACK)
     */
    private BizStatus.MessageBizType bizType;

    /**
     * 锚点目标ID (例如：具体的评论ID，用于页面内精准滚动高亮)
     */
    private Long targetId;

    /**
     * 阅读状态：0-未读，1-已读
     */
    private BizStatus.ReadStatus isRead;

}