package com.example.blog.modules.operation.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.core.exception.CustomerException;
import com.example.blog.core.security.UserContext;
import com.example.blog.modules.operation.event.FeedbackAdminNoticeEvent;
import com.example.blog.modules.operation.event.FeedbackRepliedEvent;
import com.example.blog.modules.operation.event.FeedbackReplyEmailEvent;
import com.example.blog.modules.operation.event.FeedbackSubmittedEvent;
import com.example.blog.modules.operation.mapper.FeedbackMapper;
import com.example.blog.modules.operation.model.dto.FeedbackAddDTO;
import com.example.blog.modules.operation.model.dto.FeedbackProcessDTO;
import com.example.blog.modules.operation.model.dto.FeedbackQueryDTO;
import com.example.blog.modules.operation.model.entity.Feedback;
import com.example.blog.modules.operation.model.vo.AdminFeedbackVO;
import com.example.blog.modules.operation.service.FeedbackService;
import com.example.blog.modules.user.event.UserUnbannedEvent;
import com.example.blog.modules.user.model.dto.UserPayloadDTO;
import com.example.blog.modules.user.model.entity.User;
import com.example.blog.modules.user.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    @Resource
    private UserService userService;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFeedback(FeedbackAddDTO addDTO) {
        Assert.notNull(addDTO, "反馈内容不能为空");

        if (BizStatus.FeedbackType.APPEAL.getCode().equals(addDTO.getType())) {
            String contactEmail = addDTO.getContactEmail();
            if (StrUtil.isBlank(contactEmail)) {
                throw new CustomerException(ResultCode.PARAM_ERROR, "封禁申诉必须填写联系邮箱");
            }

            // 查询该邮箱对应的用户
            User bannedUser = userService.getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, contactEmail));

            // 校验用户是否存在，以及状态是否为封禁 (DISABLE)
            if (bannedUser == null || bannedUser.getStatus() != BizStatus.User.DISABLE) {
                throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_APPEAL_EMAIL_INVALID);
            }
        }

        // DTO 转 Entity
        Feedback feedback = BeanUtil.copyProperties(addDTO, Feedback.class);

        // 尝试获取当前登录用户ID
        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser != null) {
            feedback.setUserId(currentUser.getId());
        }

        // 默认状态设为待处理
        feedback.setStatus(BizStatus.FeedbackStatus.PENDING);

        this.save(feedback);

        // 4. 如果是已登录用户，发布事件发送站内通知
        if (currentUser != null) {
            eventPublisher.publishEvent(new FeedbackSubmittedEvent(this, currentUser.getId()));
        }

        // 5. 反馈提交成功后，发布事件通知管理员
        eventPublisher.publishEvent(new FeedbackAdminNoticeEvent(this, feedback.getContent(), feedback.getContactEmail()));
    }

    @Override
    public IPage<AdminFeedbackVO> pageAdminFeedbacks(FeedbackQueryDTO queryDTO) {
        Page<Feedback> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Feedback> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(queryDTO.getStatus() != null, Feedback::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getType() != null, Feedback::getType, queryDTO.getType())
                .orderByDesc(Feedback::getCreateTime);

        // 1. 查询实体分页数据
        Page<Feedback> entityPage = this.page(page, queryWrapper);

        // 2. 转换为 VO 分页对象
        Page<AdminFeedbackVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<Feedback> records = entityPage.getRecords();

        if (CollUtil.isEmpty(records)) {
            return voPage;
        }

        // 3. 提取所有有 userId 的记录，批量查询 User 集合
        List<Long> userIds = records.stream()
                .map(Feedback::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, String> userNicknameMap = Map.of();
        if (CollUtil.isNotEmpty(userIds)) {
            List<User> users = userService.listByIds(userIds);
            userNicknameMap = users.stream().collect(Collectors.toMap(User::getId, User::getNickname));
        }

        // 4. 组装 VO 列表
        final Map<Long, String> finalUserMap = userNicknameMap;
        List<AdminFeedbackVO> voList = records.stream().map(feedback -> {
            AdminFeedbackVO vo = BeanUtil.copyProperties(feedback, AdminFeedbackVO.class);
            // 组装用户昵称
            if (vo.getUserId() != null) {
                vo.setUserNickname(finalUserMap.getOrDefault(vo.getUserId(), Constants.DEFAULT_UNKNOWN_NICKNAME));
            } else {
                vo.setUserNickname(Constants.DEFAULT_GUEST_NICKNAME);
            }
            return vo;
        }).toList();

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processFeedback(FeedbackProcessDTO processDTO) {
        Assert.notNull(processDTO, "处理参数不能为空");

        // 1. 先查询数据库中的原始记录
        Feedback originalFeedback = this.getById(processDTO.getId());
        Assert.notNull(originalFeedback, "反馈记录不存在");

        // 2. 【状态机拦截】检查当前状态是否已经是终态
        if (originalFeedback.getStatus() == BizStatus.FeedbackStatus.RESOLVED ||
                originalFeedback.getStatus() == BizStatus.FeedbackStatus.REJECTED) {
            throw new IllegalArgumentException(MessageConstants.MSG_FEEDBACK_ALREADY_TERMINATED);
        }

        BizStatus.FeedbackStatus newStatus = BizStatus.FeedbackStatus.getByCode(processDTO.getStatus());

        // 3. 更新反馈状态和回复内容
        Feedback updateEntity = new Feedback();
        updateEntity.setId(processDTO.getId());
        updateEntity.setAdminReply(processDTO.getAdminReply());
        updateEntity.setStatus(newStatus);
        this.updateById(updateEntity);

        // 如果是“封禁申诉”，并且处理结果是“已解决”（代表同意申诉）
        if (BizStatus.FeedbackType.APPEAL.equals(originalFeedback.getType())
                && BizStatus.FeedbackStatus.RESOLVED.equals(newStatus)) {

            Long targetUserId = originalFeedback.getUserId();

            // 因为封禁用户无法登录，提交申诉时 userId 通常为 null，需要通过邮箱反查用户
            if (targetUserId == null && StrUtil.isNotBlank(originalFeedback.getContactEmail())) {
                User bannedUser = userService.getOne(new LambdaQueryWrapper<User>()
                        .eq(User::getEmail, originalFeedback.getContactEmail()));
                if (bannedUser != null) {
                    targetUserId = bannedUser.getId();
                }
            }

            // 如果最终找到了对应的用户，发布解封事件
            if (targetUserId != null) {
                eventPublisher.publishEvent(new UserUnbannedEvent(this, targetUserId));
                log.info("已发布用户解封事件，触发源：同意封禁申诉，反馈ID: {}, 邮箱: {}, 解封用户ID: {}",
                        originalFeedback.getId(), originalFeedback.getContactEmail(), targetUserId);
            } else {
                log.warn("同意封禁申诉，但系统未找到对应邮箱的账号。反馈ID: {}, 邮箱: {}",
                        originalFeedback.getId(), originalFeedback.getContactEmail());
                throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_APPEAL_EMAIL_NOT_FOUND);
            }
        }

        // 4. 【按需发邮件】只有当新状态是“已解决”或“已驳回”，并且填写了回复、有邮箱时，才发送邮件
        boolean isTerminalState = (newStatus == BizStatus.FeedbackStatus.RESOLVED || newStatus == BizStatus.FeedbackStatus.REJECTED);
        String contactEmail = originalFeedback.getContactEmail();

        if (isTerminalState && StrUtil.isNotBlank(contactEmail) && Validator.isEmail(contactEmail)) {
            // 动态判断状态，赋予不同的默认回复和 UI 颜色
            String defaultReply;
            String statusText = newStatus.getDesc();
            String replyColor;

            if (newStatus == BizStatus.FeedbackStatus.RESOLVED) {
                defaultReply = MessageConstants.MSG_FEEDBACK_REPLY_RESOLVED;
                replyColor = Constants.COLOR_SUCCESS;
            } else {
                defaultReply = MessageConstants.MSG_FEEDBACK_REPLY_REJECTED;
                replyColor = Constants.COLOR_INFO;
            }

            // 注意：如果管理员没有写回复，通常不建议发空邮件，做个兜底
            String replyContent = StrUtil.isNotBlank(processDTO.getAdminReply()) ? processDTO.getAdminReply() : defaultReply;

            eventPublisher.publishEvent(new FeedbackReplyEmailEvent(
                    this, contactEmail, originalFeedback.getContent(), replyContent, statusText, replyColor
            ));

            // 5. 如果是登录用户提交的反馈，发布事件发送站内信
            if (originalFeedback.getUserId() != null) {
                eventPublisher.publishEvent(new FeedbackRepliedEvent(
                        this,
                        originalFeedback.getUserId(),
                        originalFeedback.getId(),
                        originalFeedback.getContent(),
                        replyContent,
                        newStatus
                ));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFeedbackById(Long id) {
        Assert.notNull(id, "反馈ID不能为空");

        boolean success = this.removeById(id);

        // 如果删除失败（例如 ID 不存在或已被逻辑删除），抛出业务异常给前端提示
        if (!success) {
            throw new CustomerException(MessageConstants.MSG_FEEDBACK_NOT_EXIST);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteFeedbacks(List<Long> ids) {
        Assert.notEmpty(ids, "反馈ID列表不能为空");

        boolean success = this.removeBatchByIds(ids);

        if (!success) {
            throw new CustomerException(MessageConstants.MSG_BATCH_DELETE_FAILED);
        }
    }
}