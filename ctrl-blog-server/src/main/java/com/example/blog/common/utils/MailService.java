package com.example.blog.common.utils;

import freemarker.template.Template;
import jakarta.annotation.Resource;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.Map;

/**
 * 邮件发送服务类
 * 提供基于 FreeMarker 模板的 HTML 邮件发送功能，支持异步处理
 */
@Slf4j
@Component
public class MailService {

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private FreeMarkerConfigurer freeMarkerConfigurer;

    // 获取配置文件中的发件人
    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送简单文本邮件
     * @param to 收件人
     * @param subject 主题
     * @param model 模板数据模型
     */
    @Async // 异步发送，防止发邮件的网络延迟卡住用户注册
    public void sendHtmlMail(String to, String subject, String templateName, Map<String, Object> model) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);

            // 获取 FreeMarker 模板
            Template template = freeMarkerConfigurer.getConfiguration().getTemplate(templateName);
            // 将数据模型注入模板，生成 HTML 字符串
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

            // 设置邮件内容为 HTML 格式
            helper.setText(html, true);

            javaMailSender.send(message);
            log.info("邮件发送成功：{}", to);
        } catch (Exception e) {
            log.error("邮件发送失败，目标：{}，原因：{}", to, e.getMessage());
        }
    }
}
