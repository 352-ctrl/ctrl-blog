package com.example.blog.modules.operation.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.base.BaseLogicEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 意见反馈实体类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("blog_feedback")
public class Feedback extends BaseLogicEntity {

    /**
     * 反馈用户ID (为空表示游客匿名反馈)
     */
    private Long userId;

    /**
     * 反馈类型
     */
    private BizStatus.FeedbackType type;

    /**
     * 反馈详细内容
     */
    private String content;

    /**
     * 附加截图 (JSON数组或逗号分隔)
     */
    private String images;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 处理状态
     */
    private BizStatus.FeedbackStatus status;

    /**
     * 管理员回复内容
     */
    private String adminReply;

}