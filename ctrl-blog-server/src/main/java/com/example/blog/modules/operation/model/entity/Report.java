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
 * 内容举报投诉实体类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("blog_report")
public class Report extends BaseLogicEntity {

    /**
     * 举报人ID
     */
    private Long userId;

    /**
     * 举报目标类型 (COMMENT, ARTICLE, USER)
     */
    private BizStatus.ReportTargetType targetType;

    /**
     * 举报目标ID
     */
    private Long targetId;

    /**
     * 举报原因 (如：垃圾广告、违法违规等)
     */
    private BizStatus.ReportReason reason;

    /**
     * 举报人补充的详细说明
     */
    private String content;

    /**
     * 处理状态
     */
    private BizStatus.ReportStatus status;

    /**
     * 管理员内部处理备注
     */
    private String adminNote;

}