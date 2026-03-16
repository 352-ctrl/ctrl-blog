package com.example.blog.modules.operation.listener;

import com.example.blog.common.constants.Constants;
import com.example.blog.common.utils.MailService;
import com.example.blog.modules.operation.event.FeedbackAdminNoticeEvent;
import com.example.blog.modules.operation.event.FeedbackReplyEmailEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * 反馈相关邮件的监听器
 * 确保在数据库事务真正 Commit（提交）之后，才去触发发邮件动作
 */
@Slf4j
@Component
public class FeedbackMailListener {

    @Resource
    private MailService mailService;

    // 从配置文件读取管理员接收邮件的邮箱地址
    @Value("${blog.admin.email:admin@example.com}")
    private String adminEmail;

    /**
     * 监听管理员收到新反馈事件
     * TransactionPhase.AFTER_COMMIT 确保数据库 insert 成功后才发邮件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFeedbackAdminNotice(FeedbackAdminNoticeEvent event) {
        log.info("监听到新反馈提交成功，准备发送邮件给管理员...");
        Map<String, Object> adminModel = new HashMap<>();
        adminModel.put("content", event.getContent());
        adminModel.put("contact", event.getContactEmail());

        // 调用发送（底层 MailService 已带 @Async，因此这里不会阻塞）
        mailService.sendHtmlMail(adminEmail, Constants.EMAIL_SUBJECT_FEEDBACK_ADMIN, Constants.TEMPLATE_FEEDBACK_ADMIN, adminModel);
    }

    /**
     * 监听用户收到反馈处理结果事件
     * TransactionPhase.AFTER_COMMIT 确保数据库 update 成功后才发邮件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFeedbackReplyEmail(FeedbackReplyEmailEvent event) {
        log.info("监听到反馈状态更新成功，准备发送通知邮件给用户：{}", event.getContactEmail());
        Map<String, Object> userModel = new HashMap<>();
        userModel.put("content", event.getContent());
        userModel.put("adminReply", event.getAdminReply());
        userModel.put("statusText", event.getStatusText());
        userModel.put("replyColor", event.getReplyColor());

        mailService.sendHtmlMail(event.getContactEmail(), Constants.EMAIL_SUBJECT_FEEDBACK_REPLY, Constants.TEMPLATE_FEEDBACK_REPLY, userModel);
    }
}