package com.example.blog.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.convert.UserConvert;
import com.example.blog.dto.user.UserAddDTO;
import com.example.blog.dto.user.UserPayloadDTO;
import com.example.blog.dto.user.UserQueryDTO;
import com.example.blog.dto.user.UserUpdateDTO;
import com.example.blog.entity.User;
import com.example.blog.exception.CustomerException;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.UserService;
import com.example.blog.utils.RedisUtil;
import com.example.blog.utils.UserContext;
import com.example.blog.vo.UserVO;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统用户业务服务实现类
 * 实现用户相关的具体业务逻辑
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserConvert userConvert;

    @Resource
    private RedisUtil redisUtil;

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

        if (queryDTO == null) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_PARAM_ERROR);
        }
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

        User user = userConvert.addDtoToEntity(addDTO);
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

        User user = this.getById(updateDTO.getId());
        if (user == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_USER_NOT_EXIST);
        }

        userConvert.updateEntityFromDto(updateDTO, user);
        this.updateById(user);

        // 删除 Redis 缓存
        redisUtil.delete(RedisConstants.REDIS_USER_INFO_KEY + updateDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserById(Long id) {
        Assert.notNull(id, "用户ID不能为空");

        // 防止删除自己或超级管理员
        UserPayloadDTO currentUser = UserContext.get();
        // 校验是否删除自己
        if (currentUser != null && id.equals(currentUser.getId())) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_CANNOT_DELETE_SELF);
        }
        // 校验是否删除超级管理员 (假设ID为1)
        if (Integer.valueOf(1).equals(id)) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_CANNOT_DELETE_ADMIN);
        }
        // 给要删除的数据的唯一键加上时间戳
        User user = this.getById(id);
        if (user == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_USER_NOT_EXIST);
        }
        user.setEmail(user.getEmail() + Constants.DELETE_PREFIX + System.currentTimeMillis());
        this.updateById(user);
        boolean success = this.removeById(id);
        // 删除 Redis 缓存
        if (success) {
            redisUtil.delete(RedisConstants.REDIS_USER_INFO_KEY + id);
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

        // 校验是否包含当前登录用户
        if (ids.contains(currentUser.getId())) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_CANNOT_DELETE_SELF);
        }

        // 校验是否包含超级管理员 (ID = 1)
        if (ids.contains(1)) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_CANNOT_DELETE_ADMIN);
        }

        // 给要删除的数据的唯一键加上时间戳
        List<User> users = this.listByIds(ids);
        for (User user : users) {
            user.setEmail(user.getEmail() + Constants.DELETE_PREFIX + System.currentTimeMillis());
        }
        this.updateBatchById(users);
        this.removeByIds(ids);

        // 批量删除 Redis 缓存
        List<String> keys = ids.stream()
                .map(id -> RedisConstants.REDIS_USER_INFO_KEY + id)
                .collect(Collectors.toList());
        redisUtil.delete(keys);
    }

}