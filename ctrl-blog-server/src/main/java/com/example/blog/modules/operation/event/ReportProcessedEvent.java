package com.example.blog.modules.operation.event;

import com.example.blog.modules.operation.model.entity.Report;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 举报已处理事件 (包含通知举报人和被举报人)
 */
@Getter
public class ReportProcessedEvent extends ApplicationEvent {
    private final Report report;       // 包含举报人、目标对象、处理备注等全量信息
    private final Long targetUserId;   // 被举报内容的拥有者ID（用于发警告信，可能为空）

    public ReportProcessedEvent(Object source, Report report, Long targetUserId) {
        super(source);
        this.report = report;
        this.targetUserId = targetUserId;
    }
}