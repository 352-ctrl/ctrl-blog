package com.example.blog.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.convert.UserConvert;
import com.example.blog.dto.user.UserChangePwdDTO;
import com.example.blog.dto.user.UserPayloadDTO;
import com.example.blog.dto.user.UserProfileUpdateDTO;
import com.example.blog.entity.User;
import com.example.blog.exception.CustomerException;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.AuthService;
import com.example.blog.service.UserProfileService;
import com.example.blog.utils.PasswordEncoderUtil;
import com.example.blog.utils.RedisUtil;
import com.example.blog.utils.UserContext;
import com.example.blog.vo.UserVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * 个人信息业务服务实现类
 * 实现个人信息相关的具体业务逻辑
 */
@Service
public class UserProfileServiceImpl extends ServiceImpl<UserMapper, User> implements UserProfileService {

    @Resource
    private UserConvert userConvert;

    @Resource
    private AuthService authService;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 通用用户修改操作前置校验
     * @param id 要更新的用户ID
     * @return 校验通过后的用户对象
     * @throws CustomerException 校验失败时抛出异常
     */
    private User validateUserForUpdate(Long id) {
        Assert.notNull(id, "用户ID不能为空");

        // 登录状态校验
        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null || currentUser.getId() == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        // 权限校验
        if (!id.equals(currentUser.getId())) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NO_PERMISSION);
        }

        // 验证用户是否存在
        User user = this.getById(id);
        if (user == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_USER_NOT_EXIST);
        }

        return user;
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    @Override
    public UserVO getProfile() {
        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null || currentUser.getId() == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        Long userId = currentUser.getId();
        String cacheKey = RedisConstants.REDIS_USER_INFO_KEY + userId;

        // 1. 尝试从缓存获取
        UserVO cachedUser = (UserVO) redisUtil.get(cacheKey);
        if (cachedUser != null) {
            return cachedUser;
        }

        // 2. 缓存不存在，查询数据库
        User user = this.getById(currentUser.getId());
        if (user == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_USER_NOT_EXIST);
        }

        UserVO userVO = userConvert.entityToVo(user);

        redisUtil.set(cacheKey, userVO, RedisConstants.EXPIRE_USER_INFO, TimeUnit.MINUTES);

        return userVO;
    }

    /**
     * 更新用户基本信息（个人信息设置）
     *
     * @param updateDTO 用户信息更新DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(UserProfileUpdateDTO updateDTO) {
        Assert.notNull(updateDTO, "更新用户信息参数不能为空");

        UserPayloadDTO currentUser = UserContext.get();
        // 基础校验
        if (currentUser == null || currentUser.getId() == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }
        // 检查是否有内容需要更新
        boolean hasUpdate = false;
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, currentUser.getId());
        if (StrUtil.isNotBlank(updateDTO.getNickname())) {
            updateWrapper.set(User::getNickname, updateDTO.getNickname());
            hasUpdate = true;
        }
        if (StrUtil.isNotBlank(updateDTO.getAvatar())) {
            updateWrapper.set(User::getAvatar, updateDTO.getAvatar());
            hasUpdate = true;
        }

        // 如果没有字段需要更新，直接返回，不查库不删缓存
        if (!hasUpdate) {
            return;
        }

        boolean success = this.update(null, updateWrapper);
        if (success) {
            throw new CustomerException(MessageConstants.MSG_UPDATE_FAILED);
        }

        // 删除 Redis 缓存
        redisUtil.delete(RedisConstants.REDIS_USER_INFO_KEY + currentUser.getId());
    }

    /**
     * 修改密码
     *
     * @param changePwdDTO 用户修改密码DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(UserChangePwdDTO changePwdDTO, String token) {
        Assert.notNull(changePwdDTO, "修改密码参数不能为空");

        UserPayloadDTO currentUser = UserContext.get();
        if (changePwdDTO == null || currentUser.getId() == null) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_PARAM_ERROR);
        }

        User user = validateUserForUpdate(currentUser.getId());

        // 验证原密码
        if (!PasswordEncoderUtil.matches(changePwdDTO.getOldPassword(), user.getPassword())) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_OLD_PASSWORD_ERROR);
        }

        // 验证原密码与新密码是否相同
        if (PasswordEncoderUtil.matches(changePwdDTO.getNewPassword(), user.getPassword())) {
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_NEW_PASSWORD_SAME_AS_OLD);
        }

        // 密码加密（核心：明文→BCrypt哈希）
        String encryptedPassword = PasswordEncoderUtil.encode(changePwdDTO.getNewPassword());

        // 更新密码
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getPassword, encryptedPassword)
                .eq(User::getId, currentUser.getId());

        boolean success = this.update(null, updateWrapper);
        if (!success) {
            throw new CustomerException(MessageConstants.MSG_UPDATE_FAILED);
        }

        // 删除 Redis 缓存
        redisUtil.delete(RedisConstants.REDIS_USER_INFO_KEY + currentUser.getId());

        // 调用 AuthService 的 logout 方法拉黑 Token
        if (StrUtil.isNotBlank(token)) {
            authService.logout(token);
        }
    }

}