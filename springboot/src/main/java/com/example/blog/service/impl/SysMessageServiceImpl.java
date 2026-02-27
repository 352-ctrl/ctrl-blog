package com.example.blog.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.dto.message.MessageSendDTO;
import com.example.blog.dto.user.UserPayloadDTO;
import com.example.blog.entity.SysMessage;
import com.example.blog.entity.User;
import com.example.blog.exception.CustomerException;
import com.example.blog.mapper.SysMessageMapper;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.SysMessageService;
import com.example.blog.utils.UserContext;
import com.example.blog.vo.MessageVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统消息业务服务实现类
 * 处理系统通知、点赞、评论等互动消息的核心逻辑
 */
@Slf4j
@Service
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements SysMessageService {

    @Resource
    private UserMapper userMapper;

    @Override
    public List<MessageVO> listMessages(BizStatus.MessageType type) {
        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        // 1. 构建查询条件
        LambdaQueryWrapper<SysMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysMessage::getToUserId, currentUser.getId())
                .eq(type != null, SysMessage::getType, type)
                .orderByDesc(SysMessage::getCreateTime)
                .last("LIMIT 200"); // 强制限制条数，防爆破

        List<SysMessage> messages = this.list(queryWrapper);

        if (messages.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 收集所有需要的 发送方ID
        Set<Long> fromUserIds = messages.stream()
                .map(SysMessage::getFromUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 3. 批量查询 User 信息
        final Map<Long, User> userMap;
        if (!fromUserIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(fromUserIds);
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
    public Long getUnreadMessageCount() {
        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null) {
            return 0L; // 游客状态未读数为 0
        }

        LambdaQueryWrapper<SysMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysMessage::getToUserId, currentUser.getId())
                .eq(SysMessage::getIsRead, BizStatus.ReadStatus.UNREAD);

        return this.count(queryWrapper);
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
        SysMessage message = this.getById(messageId);
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

        LambdaUpdateWrapper<SysMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SysMessage::getIsRead, BizStatus.ReadStatus.READ)
                .eq(SysMessage::getToUserId, currentUser.getId())
                .eq(SysMessage::getIsRead, BizStatus.ReadStatus.UNREAD); // 只更新状态为未读的

        // 如果指定了消息类型，则增加条件限制
        if (type != null) {
            updateWrapper.eq(SysMessage::getType, type);
        }

        this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendSystemNotice(MessageSendDTO sendDTO) {
        Assert.notNull(sendDTO.getToUserId(), "接收用户ID不能为空");
        Assert.hasText(sendDTO.getTitle(), "消息标题不能为空");

        SysMessage message = SysMessage.builder()
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
        User fromUser = userMapper.selectById(sendDTO.getFromUserId());
        // 如果用户被物理删除、注销或昵称为空，显示为"神秘用户"
        String fromNickname = (fromUser != null && StrUtil.isNotBlank(fromUser.getNickname()))
                ? fromUser.getNickname()
                : Constants.DEFAULT_UNKNOWN_NICKNAME;

        SysMessage message = SysMessage.builder()
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
}