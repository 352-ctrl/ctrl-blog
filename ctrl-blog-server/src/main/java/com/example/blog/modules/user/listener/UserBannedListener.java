package com.example.blog.modules.user.listener;

import cn.hutool.core.util.StrUtil;
import com.example.blog.common.utils.MailService;
import com.example.blog.modules.user.event.UserBannedEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户模块邮件监听器
 */
@Slf4j
@Component
public class UserMailListener {

    @Resource
    private MailService mailService;

    // 注入管理员邮箱，用于邮件模板中的申诉联系方式
    @Value("${blog.admin.email:admin@example.com}")
    private String adminEmail;

    /**
     * 监听用户被封禁事件
     * 使用 AFTER_COMMIT 确保数据库更新成功后再发邮件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserBanned(UserBannedEvent event) {
        // 如果没有邮箱，则无法发送
        if (StrUtil.isBlank(event.getEmail())) {
            log.warn("用户 {} (ID:{}) 被封禁，但未绑定邮箱，跳过邮件发送", event.getNickname(), event.getUserId());
            return;
        }

        log.info("监听到用户封禁事件，准备发送通知邮件给用户：{}", event.getEmail());

        Map<String, Object> model = new HashMap<>();
        model.put("nickname", event.getNickname());
        model.put("banReason", event.getBanReason());
        model.put("adminEmail", adminEmail);

        // 处理封禁类型和时间格式化
        String banType = "永久封禁";
        String endTimeStr = "永久封禁";

        if (event.getDisableEndTime() != null) {
            // 判断是否是你在 processReport 中设置的永久封禁极大值 (2099年)
            if (event.getDisableEndTime().getYear() < 2099) {
                banType = "限时封禁";
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                endTimeStr = event.getDisableEndTime().format(formatter);
            }
        }

        model.put("banType", banType);
        model.put("endTime", endTimeStr);

        // 调用异步邮件发送服务
        // 注意：Constants.TEMPLATE_USER_BANNED 的值应该是 "user_banned.ftl"
        mailService.sendHtmlMail(
                event.getEmail(),
                "【CtrlBlog】账号违规处理通知",
                "user_banned.ftl",
                model
        );
    }
}