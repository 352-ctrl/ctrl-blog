package com.example.blog.modules.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.modules.user.model.convert.UserConvert;
import com.example.blog.modules.user.model.dto.*;
import com.example.blog.modules.user.model.entity.User;
import com.example.blog.modules.user.event.UserBannedEvent;
import com.example.blog.modules.user.event.UserInfoChangedEvent;
import com.example.blog.modules.user.event.UserSecurityEvent;
import com.example.blog.modules.user.event.UserUnbannedEvent;
import com.example.blog.core.exception.CustomerException;
import com.example.blog.modules.user.mapper.UserMapper;
import com.example.blog.common.utils.GravatarUtils;
import com.example.blog.core.security.PasswordEncoderUtil;
import com.example.blog.common.utils.RedisUtil;
import com.example.blog.core.security.UserContext;
import com.example.blog.modules.user.service.UserService;
import com.example.blog.modules.user.model.vo.UserVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统用户业务服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserConvert userConvert;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    /**
     * 校验目标对象操作权限（防向上越权、防横向平级越权）
     */
    private void checkTargetPermission(User targetUser, UserPayloadDTO currentUser) {
        if (currentUser == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        if (BizStatus.Role.SUPER_ADMIN == currentUser.getRole()) {
            return;
        }

        if (BizStatus.Role.SUPER_ADMIN == targetUser.getRole()) {
            throw new CustomerException(ResultCode.FORBIDDEN, MessageConstants.MSG_CANNOT_OPERATE_SUPER_ADMIN);
        }

        if (BizStatus.Role.ADMIN == targetUser.getRole() && !currentUser.getId().equals(targetUser.getId())) {
            throw new CustomerException(ResultCode.FORBIDDEN, MessageConstants.MSG_CANNOT_OPERATE_OTHER_ADMIN);
        }
    }

    /**
     * 校验角色分配权限（防提权、防降权）
     */
    private void checkRoleGrantPermission(BizStatus.Role assignRole, User targetUser, UserPayloadDTO currentUser) {
        if (currentUser == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }
        if (assignRole == null) return;

        // 1. 防自降/自提
        if (targetUser != null && currentUser.getId().equals(targetUser.getId())) {
            if (assignRole != targetUser.getRole()) {
                throw new CustomerException(ResultCode.FORBIDDEN, MessageConstants.MSG_CANNOT_CHANGE_OWN_ROLE);
            }
        }

        // 2. 绝对防御
        if (BizStatus.Role.SUPER_ADMIN == assignRole) {
            throw new CustomerException(ResultCode.FORBIDDEN, MessageConstants.MSG_CANNOT_GRANT_SUPER_ADMIN);
        }

        // 3. 越级防御
        if (BizStatus.Role.SUPER_ADMIN != currentUser.getRole()) {
            if (BizStatus.Role.USER != assignRole) {
                throw new CustomerException(ResultCode.FORBIDDEN, MessageConstants.MSG_ADMIN_CANNOT_GRANT_ADMIN);
            }
        }
    }

    /**
     * 监听用户解封事件
     */
    @EventListener
    public void handleUserUnbannedEvent(UserUnbannedEvent event) {
        Long userId = event.getUserId();
        if (userId == null) {
            return;
        }

        // 1. 修改数据库状态为正常，并清空封禁信息
        this.lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getStatus, BizStatus.User.NORMAL)
                .set(User::getDisableEndTime, null)
                .set(User::getDisableReason, null)
                .update();

        // 2. 清理该用户的 Redis 缓存
        redisUtil.delete(RedisConstants.REDIS_USER_INFO_KEY + userId);

        log.info("[事件处理] 用户已成功解封并清理缓存，用户ID: {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPasswordByAdmin(AdminResetPwdDTO adminResetPwdDTO) {
        Assert.notNull(adminResetPwdDTO, "参数不能为空");

        User targetUser = this.getById(adminResetPwdDTO.getId());
        if (targetUser == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_USER_NOT_EXIST);
        }

        checkTargetPermission(targetUser, UserContext.get());

        String encryptedPassword = PasswordEncoderUtil.encode(adminResetPwdDTO.getNewPassword());
        targetUser.setPassword(encryptedPassword);
        this.updateById(targetUser);

        redisUtil.delete(RedisConstants.REDIS_USER_INFO_KEY + targetUser.getId());
        redisUtil.delete(RedisConstants.REDIS_USER_TOKEN_KEY + targetUser.getId());

        // 使用事件驱动发送系统通知
        eventPublisher.publishEvent(new UserSecurityEvent(this, targetUser.getId(), BizStatus.SecurityEventType.PASSWORD_RESET, null));
    }

    @Override
    public UserVO getUserById(Long id) {
        Assert.notNull(id, "用户ID不能为空");

        User user = this.getById(id);
        if (user == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_USER_NOT_EXIST);
        }
        return userConvert.entityToVo(user);
    }

    @Override
    public IPage<UserVO> pageAdminUsers(UserQueryDTO queryDTO) {
        Assert.notNull(queryDTO, "分页查询参数不能为空");

        Page<User> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(queryDTO.getNickname()), User::getNickname, queryDTO.getNickname())
                .orderByDesc(User::getCreateTime);
        IPage<User> userPage = this.page(page, queryWrapper);
        return userPage.convert(user -> userConvert.entityToVo(user));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(UserAddDTO addDTO) {
        Assert.notNull(addDTO, "新增用户参数不能为空");

        checkRoleGrantPermission(addDTO.getRole(), null, UserContext.get());

        User user = userConvert.addDtoToEntity(addDTO);

        if (user.getAvatar() == null) {
            // 设置默认头像
            user.setAvatar(GravatarUtils.getRetroAvatar(user.getEmail()));
        }

        try {
            this.save(user);
        } catch (DuplicateKeyException e) {
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_EMAIL_EXIST);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserUpdateDTO updateDTO) {
        Assert.notNull(updateDTO, "更新用户参数不能为空");
        Assert.notNull(updateDTO.getId(), "用户ID不能为空");

        User targetUser = this.getById(updateDTO.getId());
        if (targetUser == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_USER_NOT_EXIST);
        }

        UserPayloadDTO currentUser = UserContext.get();
        checkTargetPermission(targetUser, currentUser);

        // 1. 记录旧角色和旧状态，用于判断是否发生变更
        BizStatus.Role oldRole = targetUser.getRole();
        var oldStatus = targetUser.getStatus();

        if (updateDTO.getRole() != null && updateDTO.getRole() != oldRole) {
            checkRoleGrantPermission(updateDTO.getRole(), targetUser, currentUser);
        }

        // 更新实体并保存到数据库
        userConvert.updateEntityFromDto(updateDTO, targetUser);
        this.updateById(targetUser);

        // 清理用户信息缓存
        redisUtil.delete(RedisConstants.REDIS_USER_INFO_KEY + updateDTO.getId());

        // 2. 处理角色变更逻辑
        if (updateDTO.getRole() != null && updateDTO.getRole() != oldRole) {
            redisUtil.delete(RedisConstants.REDIS_USER_TOKEN_KEY + updateDTO.getId());
            // 发送角色变更事件
            eventPublisher.publishEvent(new UserSecurityEvent(this, updateDTO.getId(), BizStatus.SecurityEventType.ROLE_CHANGE, updateDTO.getRole()));
        }

        // 3. 检测账号封禁状态变更，发布封禁事件
        if (updateDTO.getStatus() != null
                && BizStatus.User.DISABLE.equals(updateDTO.getStatus())
                && !BizStatus.User.DISABLE.equals(oldStatus)) {

            // 强制下线（如果角色变更没清理过的话，这里再清理一次Token以防万一）
            redisUtil.delete(RedisConstants.REDIS_USER_TOKEN_KEY + updateDTO.getId());

            String reason = StrUtil.isNotBlank(targetUser.getDisableReason())
                    ? targetUser.getDisableReason()
                    : MessageConstants.MSG_SEVERE_VIOLATION;

            // 发布封禁事件，交由监听器异步发送邮件
            eventPublisher.publishEvent(new UserBannedEvent(
                    this,
                    targetUser.getId(),
                    targetUser.getEmail(),
                    targetUser.getNickname(),
                    reason,
                    targetUser.getDisableEndTime()
            ));
        }

        boolean isAdminOrSuperAdmin = BizStatus.Role.ADMIN.equals(currentUser.getRole()) ||
                BizStatus.Role.SUPER_ADMIN.equals(currentUser.getRole());

        // 4. 资料发生修改，发布事件让 ArticleService 决定是否清理缓存
        if (isAdminOrSuperAdmin && (StrUtil.isNotBlank(updateDTO.getNickname()) || StrUtil.isNotBlank(updateDTO.getAvatar()))) {
            eventPublisher.publishEvent(new UserInfoChangedEvent(this, updateDTO.getId()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserById(Long targetUserId) {
        Assert.notNull(targetUserId, "用户ID不能为空");

        User targetUser = this.getById(targetUserId);
        if (targetUser == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_USER_NOT_EXIST);
        }

        UserPayloadDTO currentUser = UserContext.get();

        if (currentUser != null && targetUserId.equals(currentUser.getId())) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_CANNOT_DELETE_SELF);
        }

        // 校验权限
        checkTargetPermission(targetUser, currentUser);

        // ==========================================
        // 数据脱敏 (匿名化处理)
        // ==========================================
        // 1. 生成唯一伪造邮箱: #deleted_1001_1712345678@null.com
        String fakeEmail = Constants.DELETE_PREFIX + targetUserId + StrUtil.UNDERLINE + System.currentTimeMillis() + Constants.DELETED_EMAIL_SUFFIX;

        targetUser.setEmail(fakeEmail);
        targetUser.setPassword(Constants.MASK_PASSWORD); // 使用常量 ****** 破坏密码
        targetUser.setNickname(Constants.DEFAULT_UNKNOWN_NICKNAME); // 账号已注销
        targetUser.setAvatar(GravatarUtils.getRetroAvatar(fakeEmail)); // 根据新生成的伪造邮箱，分配一个随机的像素头像
        targetUser.setStatus(BizStatus.User.DISABLE); // 状态设为禁用

        // 2. 将脱敏后的数据更新到数据库
        this.updateById(targetUser);

        // 3. 执行逻辑删除
        boolean success = this.lambdaUpdate()
                .eq(User::getId, targetUserId)
                .set(User::getIsDeleted, BizStatus.DeleteStatus.DELETED)
                .set(User::getDeleteTime, LocalDateTime.now())
                .update();

        if (success) {
            // 4. 清理缓存，强制下线
            redisUtil.delete(RedisConstants.REDIS_USER_INFO_KEY + targetUserId);
            redisUtil.delete(RedisConstants.REDIS_USER_TOKEN_KEY + targetUserId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUsers(List<Long> ids) {
        Assert.notEmpty(ids, "用户ID列表不能为空");

        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        if (ids.contains(currentUser.getId())) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_CANNOT_DELETE_SELF);
        }

        List<User> targetUsers = this.listByIds(ids);

        long timestamp = System.currentTimeMillis();

        // 校验权限并执行脱敏赋值
        for (User targetUser : targetUsers) {
            checkTargetPermission(targetUser, currentUser);

            // 生成唯一伪造邮箱
            String fakeEmail = Constants.DELETE_PREFIX + targetUser.getId() + StrUtil.UNDERLINE + timestamp + Constants.DELETED_EMAIL_SUFFIX;

            targetUser.setEmail(fakeEmail);
            targetUser.setPassword(Constants.MASK_PASSWORD);
            targetUser.setNickname(Constants.DEFAULT_UNKNOWN_NICKNAME);
            targetUser.setAvatar(GravatarUtils.getRetroAvatar(fakeEmail));
            targetUser.setStatus(BizStatus.User.DISABLE);
        }

        // 1. 批量更新脱敏数据
        this.updateBatchById(targetUsers);

        // 2. 批量执行逻辑删除
        boolean success = this.lambdaUpdate()
                .in(User::getId, ids)
                .set(User::getIsDeleted, BizStatus.DeleteStatus.DELETED)
                .set(User::getDeleteTime, LocalDateTime.now())
                .update();

        // 3. 批量清理缓存
        if (success) {
            List<String> keysInfo = ids.stream().map(id -> RedisConstants.REDIS_USER_INFO_KEY + id).collect(Collectors.toList());
            List<String> keysToken = ids.stream().map(id -> RedisConstants.REDIS_USER_TOKEN_KEY + id).collect(Collectors.toList());
            redisUtil.delete(keysInfo);
            redisUtil.delete(keysToken);
        }
    }
}