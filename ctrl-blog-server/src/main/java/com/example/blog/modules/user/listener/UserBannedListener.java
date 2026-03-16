package com.example.blog.modules.user.listener;

import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.utils.MailService;
import com.example.blog.modules.user.event.UserBannedEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户违规封禁事件监听器
 */
@Slf4j
@Component
public class UserBannedListener {

    @Resource
    private MailService mailService;

    // 从配置文件读取管理员邮箱，用于在邮件中展示给用户申诉
    @Value("${blog.admin.email:admin@example.com}")
    private String adminEmail;

    /**
     * 监听用户封禁事件
     * 必须等事务提交后 (AFTER_COMMIT) 才发邮件，防止数据库回滚导致“幽灵邮件”
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserBanned(UserBannedEvent event) {
        log.info("监听到用户封禁事件，准备发送通知邮件给用户：{}", event.getEmail());

        Map<String, Object> model = new HashMap<>();

        // 1. 对应 FTL: ${nickname}
        model.put("nickname", event.getNickname());

        // 2. 对应 FTL: ${banType}。这里固定为账号封禁
        model.put("banType", MessageConstants.MSG_BAN_TYPE_ACCOUNT);

        // 3. 对应 FTL: ${banReason}
        model.put("banReason", event.getBanReason());

        // 4. 对应 FTL: ${adminEmail}
        model.put("adminEmail", adminEmail);

        // 5. 对应 FTL: ${endTime}。需要将 LocalDateTime 格式化为字符串，或者判断是否为永久封禁
        String endTimeStr = formatEndTime(event.getDisableEndTime());
        model.put("endTime", endTimeStr);

        // 异步发送邮件 (MailService.sendHtmlMail 内部应该已经标了 @Async)
        mailService.sendHtmlMail(
                event.getEmail(),
                Constants.EMAIL_SUBJECT_USER_BANNED,
                Constants.TEMPLATE_USER_BANNED,
                model
        );
    }

    /**
     * 格式化封禁结束时间
     */
    private String formatEndTime(LocalDateTime endTime) {
        if (endTime == null) {
            return MessageConstants.MSG_PERMANENT_BAN;
        }
        // 如果年份大于等于 2099，通常业务上代表永久封禁
        if (endTime.getYear() >= 2099) {
            return MessageConstants.MSG_PERMANENT_BAN;
        }
        // 格式化为：2026-03-15 15:30:00
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.FORMAT_DATETIME);
        return endTime.format(formatter);
    }
}