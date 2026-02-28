package com.example.blog.service.impl;

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
import com.example.blog.convert.UserConvert;
import com.example.blog.dto.message.MessageSendDTO;
import com.example.blog.dto.user.*;
import com.example.blog.entity.Article;
import com.example.blog.entity.User;
import com.example.blog.exception.CustomerException;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.ArticleService;
import com.example.blog.service.SysMessageService;
import com.example.blog.service.UserService;
import com.example.blog.utils.GravatarUtils;
import com.example.blog.utils.PasswordEncoderUtil;
import com.example.blog.utils.RedisUtil;
import com.example.blog.utils.UserContext;
import com.example.blog.vo.user.UserVO;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统用户业务服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserConvert userConvert;

    @Resource
    private ArticleService articleService;

    @Resource
    private SysMessageService sysMessageService;

    @Resource
    private RedisUtil redisUtil;

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

        // 发送密码重置系统通知
        sysMessageService.sendSystemNotice(
                MessageSendDTO.builder()
                        .toUserId(targetUser.getId())
                        .title(MessageConstants.TITLE_PWD_RESET)
                        .content(MessageConstants.CONTENT_PWD_RESET)
                        .build()
        );
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

        // 记录旧角色，用于判断是否发生变更
        BizStatus.Role oldRole = targetUser.getRole();

        if (updateDTO.getRole() != null && updateDTO.getRole() != oldRole) {
            checkRoleGrantPermission(updateDTO.getRole(), targetUser, currentUser);
        }

        userConvert.updateEntityFromDto(updateDTO, targetUser);
        this.updateById(targetUser);

        redisUtil.delete(RedisConstants.REDIS_USER_INFO_KEY + updateDTO.getId());
        // 如果角色变更，强制下线
        if (updateDTO.getRole() != null && updateDTO.getRole() != oldRole) {
            redisUtil.delete(RedisConstants.REDIS_USER_TOKEN_KEY + updateDTO.getId());

            // 发送角色变更系统通知
            sysMessageService.sendSystemNotice(
                    MessageSendDTO.builder()
                            .toUserId(updateDTO.getId())
                            .title(MessageConstants.TITLE_ROLE_CHANGE)
                            .content(String.format(MessageConstants.CONTENT_ROLE_CHANGE, updateDTO.getRole().getDesc()))
                            .build()
            );
        }

        // 只有管理员和超级管理员修改资料，才有可能影响文章展示，才去清理文章缓存
        boolean isAdminOrSuperAdmin = BizStatus.Role.ADMIN.equals(currentUser.getRole()) ||
                BizStatus.Role.SUPER_ADMIN.equals(currentUser.getRole());

        // 如果修改了昵称或头像，清除关联的文章缓存
        if (isAdminOrSuperAdmin && (StrUtil.isNotBlank(updateDTO.getNickname()) || StrUtil.isNotBlank(updateDTO.getAvatar()))) {

            // 清除公共文章列表缓存
            redisUtil.delete(RedisConstants.REDIS_ARTICLE_LIST_FIRST_PAGE_KEY);
            redisUtil.delete(RedisConstants.REDIS_ARTICLE_HOT_KEY);
            redisUtil.delete(RedisConstants.REDIS_ARTICLE_CAROUSEL_KEY);

            // 清除该用户的文章详情缓存
            List<Article> userArticles = articleService.list(
                    new LambdaQueryWrapper<Article>()
                            .select(Article::getId)
                            .eq(Article::getUserId, updateDTO.getId())
            );

            if (userArticles != null && !userArticles.isEmpty()) {
                List<String> articleCacheKeys = userArticles.stream()
                        .map(article -> RedisConstants.REDIS_ARTICLE_DETAIL_PREFIX + article.getId())
                        .collect(Collectors.toList());
                redisUtil.delete(articleCacheKeys);
            }
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

        checkTargetPermission(targetUser, currentUser);

        targetUser.setEmail(targetUser.getEmail() + Constants.DELETE_PREFIX + System.currentTimeMillis());
        this.updateById(targetUser);
        boolean success = this.removeById(targetUserId);

        if (success) {
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

        for (User targetUser : targetUsers) {
            checkTargetPermission(targetUser, currentUser);
        }

        for (User targetUser : targetUsers) {
            targetUser.setEmail(targetUser.getEmail() + Constants.DELETE_PREFIX + System.currentTimeMillis());
        }

        this.updateBatchById(targetUsers);
        this.removeByIds(ids);

        List<String> keysInfo = ids.stream().map(id -> RedisConstants.REDIS_USER_INFO_KEY + id).collect(Collectors.toList());
        List<String> keysToken = ids.stream().map(id -> RedisConstants.REDIS_USER_TOKEN_KEY + id).collect(Collectors.toList());
        redisUtil.delete(keysInfo);
        redisUtil.delete(keysToken);
    }
}