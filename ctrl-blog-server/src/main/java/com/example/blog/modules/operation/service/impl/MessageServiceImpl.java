package com.example.blog.modules.operation.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.modules.operation.model.dto.MessageSendDTO;
import com.example.blog.modules.user.model.dto.UserPayloadDTO;
import com.example.blog.modules.operation.model.entity.Message;
import com.example.blog.modules.operation.model.entity.Report;
import com.example.blog.modules.user.model.entity.User;
import com.example.blog.modules.article.event.ArticlesDeletedEvent;
import com.example.blog.modules.operation.event.FeedbackRepliedEvent;
import com.example.blog.modules.operation.event.InteractiveMessageEvent;
import com.example.blog.modules.operation.event.ReportProcessedEvent;
import com.example.blog.modules.user.event.UserRegisteredEvent;
import com.example.blog.modules.user.event.UserSecurityEvent;
import com.example.blog.core.exception.CustomerException;
import com.example.blog.modules.operation.mapper.MessageMapper;
import com.example.blog.modules.operation.service.MessageService;
import com.example.blog.modules.user.service.UserService;
import com.example.blog.core.security.UserContext;
import com.example.blog.modules.operation.model.vo.MessageVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统消息业务服务实现类
 * 处理系统通知、点赞、评论等互动消息的核心逻辑
 */
@Slf4j
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Resource
    private UserService userService;

    // 通过 @Lazy 延迟注入自己，打破循环依赖
    @Resource
    @Lazy
    private MessageService self;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void onUserRegistered(UserRegisteredEvent event) {
        self.sendSystemNotice(
                MessageSendDTO.builder()
                        .toUserId(event.getUserId())
                        .title(MessageConstants.TITLE_WELCOME)
                        .content(MessageConstants.CONTENT_WELCOME)
                        .build()
        );
    }

    /**
     * 监听管理员回复反馈事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void onFeedbackReplied(FeedbackRepliedEvent event) {
        log.info("监听到反馈回复事件，准备发送通知。接收人ID: {}", event.getUserId());

        // 截断一下原始内容防止标题太长
        String shortContent = StrUtil.maxLength(event.getContent(), 15);
        String statusText = event.getStatus().getDesc();

        self.sendSystemNotice(
                MessageSendDTO.builder()
                        .toUserId(event.getUserId())
                        .title(MessageConstants.TITLE_FEEDBACK_REPLY)
                        .content(String.format(MessageConstants.CONTENT_FEEDBACK_REPLY, shortContent, statusText, event.getAdminReply()))
                        .bizType(BizStatus.MessageBizType.FEEDBACK)
                        .bizId(event.getFeedbackId())
                        .build()
        );
    }

    /**
     * 监听管理员处理举报事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void onReportProcessed(ReportProcessedEvent event) {
        Report report = event.getReport();
        Long reporterId = report.getUserId();
        String adminNote = StrUtil.isBlank(report.getAdminNote()) ? "无" : report.getAdminNote();

        // 1. 发送通知给【举报人】
        if (reporterId != null) {
            String content = BizStatus.ReportStatus.VALID.equals(report.getStatus())
                    ? MessageConstants.CONTENT_REPORT_VALID
                    : MessageConstants.CONTENT_REPORT_INVALID;

            self.sendSystemNotice(
                    MessageSendDTO.builder()
                            .toUserId(reporterId)
                            .title(MessageConstants.TITLE_REPORT_RESULT)
                            .content(String.format(content, adminNote))
                            .build()
            );
        }

        // 2. 如果举报属实，且找到了被处罚的【违规人】，发送系统警告
        if (BizStatus.ReportStatus.VALID.equals(report.getStatus()) && event.getTargetUserId() != null) {
            // 避免自己举报自己导致收到两条冲突的信息
            if (!event.getTargetUserId().equals(reporterId)) {
                String targetTypeDesc = report.getTargetType().getDesc(); // 例如："评论" 或 "文章"
                this.sendSystemNotice(
                        MessageSendDTO.builder()
                                .toUserId(event.getTargetUserId())
                                .title(MessageConstants.TITLE_SYSTEM_WARNING)
                                .content(String.format(MessageConstants.CONTENT_SYSTEM_WARNING, targetTypeDesc, adminNote))
                                .build()
                );
            }
        }
    }

    /**
     * 监听互动消息事件（点赞、评论）
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class) // 开启新事务，避免受外层事务已提交的影响
    public void onInteractiveMessageEvent(InteractiveMessageEvent event) {
        log.info("监听到互动消息事件，准备发送通知。接收人ID: {}", event.getToUserId());

        // 复用原来底层的发信逻辑
        self.sendInteractiveMessage(
                MessageSendDTO.builder()
                        .toUserId(event.getToUserId())
                        .fromUserId(event.getFromUserId())
                        .type(event.getType())
                        .bizId(event.getBizId())
                        .bizType(event.getBizType())
                        .targetId(event.getTargetId())
                        .content(event.getContent())
                        .build()
        );
    }

    /**
     * 监听用户安全相关事件（修改密码、修改角色）
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class) // 强制开启独立的新事务，解决自调用和连接状态问题
    public void onUserSecurityEvent(UserSecurityEvent event) {
        log.info("监听到用户安全变更事件，准备发送通知。用户ID: {}", event.getUserId());

        // 告别魔法值，使用枚举的 equals 比较
        if (BizStatus.SecurityEventType.PASSWORD_RESET.equals(event.getEventType())) {
            self.sendSystemNotice(
                    MessageSendDTO.builder()
                            .toUserId(event.getUserId())
                            .title(MessageConstants.TITLE_PWD_RESET)
                            .content(MessageConstants.CONTENT_PWD_RESET)
                            .build()
            );
        } else if (BizStatus.SecurityEventType.ROLE_CHANGE.equals(event.getEventType()) && event.getNewRole() != null) {
            self.sendSystemNotice(
                    MessageSendDTO.builder()
                            .toUserId(event.getUserId())
                            .title(MessageConstants.TITLE_ROLE_CHANGE)
                            .content(String.format(MessageConstants.CONTENT_ROLE_CHANGE, event.getNewRole().getDesc()))
                            .build()
            );
        }
    }

    /**
     * 监听文章被删除事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onArticlesDeletedEvent(ArticlesDeletedEvent event) {
        log.info("监听到文章删除事件，准备发送通知。");
        Map<Long, String> deletedMap = event.getDeletedArticlesMap();
        List<Message> messageList = new ArrayList<>();

        deletedMap.forEach((userId, titles) -> {
            Message msg = Message.builder()
                    .toUserId(userId)
                    .fromUserId(null)
                    .type(BizStatus.MessageType.SYSTEM)
                    .title(MessageConstants.TITLE_ARTICLE_DELETE)
                    .content(String.format(MessageConstants.CONTENT_ARTICLE_DELETE, titles))
                    .isRead(BizStatus.ReadStatus.UNREAD)
                    .build();
            messageList.add(msg);
        });

        if (CollUtil.isNotEmpty(messageList)) {
            self.saveBatch(messageList);
        }
    }

    @Override
    public List<MessageVO> listMessages(BizStatus.MessageType type) {
        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        // 1. 构建查询条件
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getToUserId, currentUser.getId())
                .eq(type != null, Message::getType, type)
                .orderByDesc(Message::getCreateTime)
                .last("LIMIT 200"); // 强制限制条数，防爆破

        List<Message> messages = this.list(queryWrapper);

        if (CollUtil.isEmpty(messages)) {
            return Collections.emptyList();
        }

        // 2. 收集所有需要的 发送方ID
        Set<Long> fromUserIds = messages.stream()
                .map(Message::getFromUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 3. 批量查询 User 信息
        final Map<Long, User> userMap;
        if (CollUtil.isNotEmpty(fromUserIds)) {
            List<User> users = userService.listByIds(fromUserIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        } else {
            userMap = Collections.emptyMap(); // 如果为空，赋予一个不可变的空 Map
        }

        // 4. 组装 VO 列表返回
        return messages.stream().map(msg -> {
            MessageVO vo = MessageVO.builder()
                    .id(msg.getId())
                    .fromUserId(msg.getFromUserId())
                    .type(msg.getType())
                    .title(msg.getTitle())
                    .content(msg.getContent())
                    .bizId(msg.getBizId())
                    .bizType(msg.getBizType())
                    .targetId(msg.getTargetId()) // 组装 targetId 返回给前端
                    .isRead(msg.getIsRead())
                    .createTime(msg.getCreateTime())
                    .build();

            // 如果有发送方，填充头像和昵称
            if (msg.getFromUserId() != null && userMap.containsKey(msg.getFromUserId())) {
                User fromUser = userMap.get(msg.getFromUserId());
                vo.setFromUserNickname(fromUser.getNickname());
                vo.setFromUserAvatar(fromUser.getAvatar());
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Integer getUnreadMessageCount() {
        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null) {
            return 0; // 游客状态未读数为 0
        }

        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getToUserId, currentUser.getId())
                .eq(Message::getIsRead, BizStatus.ReadStatus.UNREAD);

        return Math.toIntExact(this.count(queryWrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markMessageAsRead(Long messageId) {
        Assert.notNull(messageId, "消息ID不能为空");

        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        // 1. 查询消息确认存在，并且确认接收方是当前登录用户（防止越权修改别人的消息状态）
        Message message = this.getById(messageId);
        if (message == null || !message.getToUserId().equals(currentUser.getId())) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_MESSAGE_NOT_FOUND);
        }

        // 2. 如果已经是已读状态，直接返回，避免不必要的数据库更新
        if (BizStatus.ReadStatus.READ.equals(message.getIsRead())) {
            return;
        }

        // 3. 更新为已读状态
        message.setIsRead(BizStatus.ReadStatus.READ);
        this.updateById(message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(BizStatus.MessageType type) {
        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Message::getIsRead, BizStatus.ReadStatus.READ)
                .eq(Message::getToUserId, currentUser.getId())
                .eq(Message::getIsRead, BizStatus.ReadStatus.UNREAD); // 只更新状态为未读的

        // 如果指定了消息类型，则增加条件限制
        if (type != null) {
            updateWrapper.eq(Message::getType, type);
        }

        this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendSystemNotice(MessageSendDTO sendDTO) {
        Assert.notNull(sendDTO.getToUserId(), "接收用户ID不能为空");
        Assert.hasText(sendDTO.getTitle(), "消息标题不能为空");

        Message message = Message.builder()
                .toUserId(sendDTO.getToUserId())
                .fromUserId(null) // 系统通知没有具体的发送人
                .type(BizStatus.MessageType.SYSTEM)
                .title(sendDTO.getTitle())
                .content(sendDTO.getContent())
                .bizId(sendDTO.getBizId())
                .bizType(sendDTO.getBizType())
                .targetId(sendDTO.getTargetId())
                .isRead(BizStatus.ReadStatus.UNREAD)
                .build();

        this.save(message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendInteractiveMessage(MessageSendDTO sendDTO) {
        Assert.notNull(sendDTO.getToUserId(), "接收用户ID不能为空");
        Assert.notNull(sendDTO.getFromUserId(), "发送用户ID不能为空");
        Assert.notNull(sendDTO.getType(), "消息大类不能为空");
        Assert.notNull(sendDTO.getBizType(), "关联业务类型不能为空");

        // 如果是自己触发的动作（如：自己点赞自己的文章，自己回复自己的评论），则不发送通知
        if (sendDTO.getToUserId().equals(sendDTO.getFromUserId())) {
            return;
        }

        // 查询触发动作的用户(发送方)的昵称
        User fromUser = userService.getById(sendDTO.getFromUserId());
        // 如果用户被物理删除、注销或昵称为空，显示为"神秘用户"
        String fromNickname = (fromUser != null && StrUtil.isNotBlank(fromUser.getNickname()))
                ? fromUser.getNickname()
                : Constants.DEFAULT_UNKNOWN_NICKNAME;

        Message message = Message.builder()
                .toUserId(sendDTO.getToUserId())
                .fromUserId(sendDTO.getFromUserId())
                .type(sendDTO.getType())
                .bizId(sendDTO.getBizId())
                .bizType(sendDTO.getBizType())
                .targetId(sendDTO.getTargetId()) // 注入 targetId
                .content(sendDTO.getContent())
                .isRead(BizStatus.ReadStatus.UNREAD)
                .build();

        // 针对点赞和评论，设定一些默认的标题或提示逻辑
        if (BizStatus.MessageType.LIKE.equals(sendDTO.getType())) {
            message.setTitle(String.format(MessageConstants.TITLE_NEW_LIKE, fromNickname));
        } else if (BizStatus.MessageType.COMMENT.equals(sendDTO.getType())) {
            message.setTitle(String.format(MessageConstants.TITLE_NEW_COMMENT, fromNickname));
        }

        this.save(message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int clearMessageTrash(LocalDateTime recycleLimitDate) {
        if (recycleLimitDate == null) {
            return 0;
        }
        // 直接调用底层 Mapper 进行物理删除
        return this.baseMapper.physicalDeleteExpired(recycleLimitDate);
    }
}