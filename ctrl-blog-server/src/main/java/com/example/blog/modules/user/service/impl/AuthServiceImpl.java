package com.example.blog.modules.user.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.common.utils.GravatarUtils;
import com.example.blog.common.utils.IpUtils;
import com.example.blog.common.utils.MailService;
import com.example.blog.common.utils.RedisUtil;
import com.example.blog.core.exception.CustomerException;
import com.example.blog.core.security.PasswordEncoderUtil;
import com.example.blog.core.security.TokenUtils;
import com.example.blog.core.security.UserContext;
import com.example.blog.modules.operation.model.dto.EmailRequestDTO;
import com.example.blog.modules.system.event.LoginLogEvent;
import com.example.blog.modules.user.event.UserRegisteredEvent;
import com.example.blog.modules.user.model.convert.UserConvert;
import com.example.blog.modules.user.model.dto.UserForgotPwdDTO;
import com.example.blog.modules.user.model.dto.UserLoginDTO;
import com.example.blog.modules.user.model.dto.UserRegisterDTO;
import com.example.blog.modules.user.model.entity.User;
import com.example.blog.modules.user.model.vo.UserLoginVO;
import com.example.blog.modules.user.model.vo.UserVO;
import com.example.blog.modules.user.service.AuthService;
import com.example.blog.modules.user.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private UserConvert userConvert;

    @Resource
    private UserService userService;

    @Resource
    private MailService mailService;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 统一获取网络信息并记录认证日志
     */
    private void recordAuthLog(String email, Integer status, String msg) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        String ip = IpUtils.getIpAddr(request);
        String userAgent = request != null ? request.getHeader(Constants.HEADER_USER_AGENT) : StrUtil.EMPTY;

        eventPublisher.publishEvent(new LoginLogEvent(this, email, status, msg, ip, userAgent));
    }

    /**
     * 统一构建登录/注册成功后的返回对象
     * 包含生成 Token、存入 Redis、记录成功日志、封装 VO
     */
    private UserLoginVO buildLoginResult(User user, boolean isRestored, String logMsg) {
        // 1. 创建 token
        String token = TokenUtils.getToken(user);

        // 2. 将 Token 存入 Redis，用于踢人或退出登录 (有效期 1 天)
        redisUtil.set(RedisConstants.REDIS_USER_TOKEN_KEY + user.getId(), token, RedisConstants.EXPIRE_USER_TOKEN, TimeUnit.DAYS);

        // 3. 记录成功日志
        recordAuthLog(user.getEmail(), BizStatus.Log.SUCCESS.getValue(), logMsg);

        // 4. 封装返回视图对象
        UserVO userVO = userConvert.entityToVo(user);
        userVO.setCreateTime(null);

        return UserLoginVO.builder()
                .token(token)
                .userInfo(userVO)
                .isRestored(isRestored)
                .build();
    }

    /**
     * 记录登录失败次数
     */
    private long recordLoginFailed(String key) {
        Long countObj = redisUtil.incrementStr(key, 1);
        long count = countObj != null ? countObj : 1L;

        if (count == 1) {
            redisUtil.expire(key, RedisConstants.EXPIRE_LOGIN_FAIL_WINDOW, TimeUnit.MINUTES);
        } else if (count == 5) {
            // == 5 时才设置锁定时间，防止恶意用户不断重置锁定倒计时
            redisUtil.expire(key, RedisConstants.LOGIN_LOCKED_TIME, TimeUnit.MINUTES);
        }
        return count;
    }

    /**
     * 统一处理验证码的防刷、生成、Redis缓存和邮件发送
     */
    private void doSendEmailCode(String email, String redisKeyPrefix, String subject) {
        // 1. 防刷校验
        String redisKey = redisKeyPrefix + email;
        long expire = java.util.Optional.ofNullable(redisUtil.getExpire(redisKey, TimeUnit.SECONDS)).orElse(-2L);

        // 当Key不存在时，expire 返回 -2。如果 > 240 说明刚发过不到1分钟
        if (expire > 240) {
            throw new CustomerException(ResultCode.TOO_MANY_REQUESTS, MessageConstants.MSG_SEND_FREQUENTLY);
        }

        // 2. 生成 6 位数字验证码
        String code = RandomUtil.randomNumbers(6);

        // 3. 存入 Redis，有效期 5 分钟
        redisUtil.setStr(redisKey, code, RedisConstants.EXPIRE_EMAIL_CODE, TimeUnit.MINUTES);

        // 4. 封装参数并异步发送邮件
        Map<String, Object> model = new HashMap<>();
        model.put("code", code);
        model.put("title", subject);
        mailService.sendHtmlMail(email, subject, Constants.TEMPLATE_REGISTER_CODE, model);
    }

    /**
     * 私有辅助方法：解析 Token 并将其加入 Redis 黑名单
     * @param token 需要拉黑的 JWT 字符串
     */
    private void blacklistToken(String token) {
        if (StrUtil.isBlank(token)) {
            return;
        }
        try {
            // 解析 Token 获取过期时间
            DecodedJWT jwt = JWT.decode(token);
            long remainingTime = jwt.getExpiresAt().getTime() - System.currentTimeMillis();

            // 如果 Token 尚未过期，则将其拉入黑名单
            if (remainingTime > 0) {
                String blacklistKey = RedisConstants.REDIS_TOKEN_BLACKLIST_PREFIX + token;
                // 将 Token 存入 Redis，过期时间设为 Token 的剩余寿命
                redisUtil.set(blacklistKey, RedisConstants.REDIS_TOKEN_BLACKLIST_VALUE, remainingTime, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            // 解析失败说明 Token 本身无效或已损坏，无需额外处理
        }
    }

    /**
     * 提取重置密码成功后的 Redis 清理逻辑
     */
    private void executeResetPasswordCacheClear(String redisKey, Long userId) {
        // 删除验证码缓存
        redisUtil.delete(redisKey);

        // 踢出该用户，强制重新登录
        // 清除用户的基本信息缓存
        redisUtil.delete(RedisConstants.REDIS_USER_INFO_KEY + userId);

        // 获取该用户当前关联的 Token，并将其拉入黑名单
        String tokenKey = RedisConstants.REDIS_USER_TOKEN_KEY + userId;
        Object oldTokenObj = redisUtil.get(tokenKey);

        if (oldTokenObj != null) {
            this.blacklistToken(oldTokenObj.toString());
            // 从 Redis 中彻底移除该用户的在线 Token 记录
            redisUtil.delete(tokenKey);
        }
    }

    @Override
    public void sendRegisterEmailCode(EmailRequestDTO emailRequestDTO) {
        Assert.notNull(emailRequestDTO, "邮箱请求参数不能为空");
        String email = emailRequestDTO.getEmail();

        // 1. 业务校验：注册时邮箱不能已存在
        long count = userService.count(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (count > 0) {
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_EMAIL_EXIST);
        }

        // 2. 调用公共私有方法发送邮件，传入明确的 Key 和 标题
        doSendEmailCode(
                email,
                RedisConstants.REDIS_EMAIL_REGISTER_CODE_KEY,
                Constants.EMAIL_SUBJECT_REGISTER
        );
    }

    @Override
    public void sendForgotPwdEmailCode(EmailRequestDTO emailRequestDTO) {
        Assert.notNull(emailRequestDTO, "邮箱请求参数不能为空");
        String email = emailRequestDTO.getEmail();

        // 1. 业务校验：找回密码时邮箱必须存在
        long count = userService.count(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (count == 0) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_USER_NOT_EXIST);
        }

        // 2. 调用公共私有方法发送邮件，传入明确的 Key 和 标题
        doSendEmailCode(
                email,
                RedisConstants.REDIS_EMAIL_RESET_CODE_KEY,
                Constants.EMAIL_SUBJECT_RESET
        );
    }

    @Override
    public UserLoginVO login(UserLoginDTO loginDTO) {
        Assert.notNull(loginDTO, "登录参数不能为空");

        String email = loginDTO.getEmail().toLowerCase().trim();
        // 检查是否被锁定
        String failKey = RedisConstants.REDIS_LOGIN_FAIL_KEY + email;

        if (Boolean.TRUE.equals(redisUtil.hasKey(failKey))) {
            String countStr = redisUtil.getStr(failKey);
            int failCount = Convert.toInt(countStr, 0);

            if (failCount >= 5) {
                // 获取剩余锁定时间，给前端展示“请xx分钟后再试”
                long expire = java.util.Optional.ofNullable(redisUtil.getExpire(failKey, TimeUnit.MINUTES)).orElse(-2L);
                // 防御性处理：万一刚好过期，给个默认值 1
                long displayTime = expire > 0 ? (expire + 1) : 1;
                // 格式化字符串
                String msg = String.format(MessageConstants.MSG_LOGIN_LOCKED, displayTime);
                // 记录因为锁定导致的登录失败
                recordAuthLog(email, BizStatus.Log.FAIL.getValue(), Constants.LOG_LOGIN_LOCKED);
                // 抛出带有动态时间的异常
                throw new CustomerException(ResultCode.ACCOUNT_LOCKED, msg);
            }
        }
        // 查询用户
        User dbUser = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (dbUser == null) {
            recordAuthLog(email, BizStatus.Log.FAIL.getValue(), MessageConstants.MSG_USER_NOT_EXIST);
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_LOGIN_ERROR);
        }

        // 验证密码
        if (!PasswordEncoderUtil.matches(loginDTO.getPassword(), dbUser.getPassword())) {
            // 记录并获取最新失败次数
            long currentFailCount = recordLoginFailed(failKey);
            if (currentFailCount >= 5) {
                // 如果正好达到 5 次，直接抛出锁定异常
                recordAuthLog(email, BizStatus.Log.FAIL.getValue(), Constants.LOG_LOGIN_LOCKED);
                // 锁定 30 分钟
                throw new CustomerException(ResultCode.ACCOUNT_LOCKED, String.format(MessageConstants.MSG_LOGIN_LOCKED, RedisConstants.LOGIN_LOCKED_TIME));
            } else {
                // 还没到 5 次，抛出普通的密码错误
                recordAuthLog(email, BizStatus.Log.FAIL.getValue(), Constants.LOG_LOGIN_PWD_ERROR);
                throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_LOGIN_ERROR);
            }
        }

        // 判断是否封禁
        if (dbUser.getStatus() == BizStatus.User.DISABLE) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime disableEndTime = dbUser.getDisableEndTime();

            // 1. 判断是否刑满释放 (有封禁时间，且当前时间已经超过了封禁时间)
            if (disableEndTime != null && now.isAfter(disableEndTime)) {
                // 1.1 更新数据库，恢复正常状态并清空封禁时间
                LambdaUpdateWrapper<User> unbanWrapper = new LambdaUpdateWrapper<>();
                unbanWrapper.set(User::getStatus, BizStatus.User.NORMAL)
                        .set(User::getDisableEndTime, null)
                        .set(User::getDisableReason, null)
                        .eq(User::getId, dbUser.getId());
                userService.update(unbanWrapper);

                // 1.2 同步更新内存里的 dbUser 对象，保证后续生成 Token 等流程顺利进行
                dbUser.setStatus(BizStatus.User.NORMAL);
                dbUser.setDisableEndTime(null);
                dbUser.setDisableReason(null);

                // 此时代码会继续往下走，正常登录成功
            } else {
                // 2. 还在封禁期内 (或者没有封禁时间但状态是禁用)
                recordAuthLog(email, BizStatus.Log.FAIL.getValue(), Constants.LOG_LOGIN_BANNED);

                String banMsg = MessageConstants.MSG_ACCOUNT_BANNED; // 默认提示：“账号已被禁用”

                // 获取封禁原因
                String reason = StrUtil.isNotBlank(dbUser.getDisableReason()) ? dbUser.getDisableReason() : StrUtil.EMPTY;

                String displayReason = StrUtil.isNotBlank(reason) ? reason : Constants.DEFAULT_NO_REASON;

                if (disableEndTime != null) {
                    // 如果年份极大，视为永久封禁
                    if (disableEndTime.getYear() > Constants.PERMANENT_BAN_YEAR_THRESHOLD) {
                        banMsg = String.format(MessageConstants.MSG_ACCOUNT_BANNED_PERMANENT, displayReason);
                    } else {
                        // 格式化时间给用户看
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.FORMAT_DATETIME_SHORT);
                        banMsg = String.format(MessageConstants.MSG_ACCOUNT_BANNED_TEMPORARY, disableEndTime.format(formatter), displayReason);
                    }
                } else {
                    // 如果没有明确的时间，只拼接原因
                    if (StrUtil.isNotBlank(reason)) {
                        banMsg += Constants.BAN_REASON_PREFIX + reason;
                    }
                }
                throw new CustomerException(ResultCode.ACCOUNT_BANNED, banMsg);
            }
        }

        boolean isRestored = false; // 标记是否触发了恢复
        if (dbUser.getStatus() == BizStatus.User.PENDING_DELETION) {
            LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(User::getStatus, BizStatus.User.NORMAL)
                    .set(User::getCancelTime, null)
                    .eq(User::getId, dbUser.getId());

            userService.update(updateWrapper);

            // 同步更新内存里的 dbUser 对象，保证后续流程拿到的数据是最新的
            dbUser.setStatus(BizStatus.User.NORMAL);
            dbUser.setCancelTime(null);

            isRestored = true; // 标记为已恢复
        }

        // 登录成功，清除失败记录
        redisUtil.delete(failKey);

        // 调用私有方法统一构建返回值
        return buildLoginResult(dbUser, isRestored, Constants.LOG_LOGIN_SUCCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserLoginVO register(UserRegisterDTO registerDTO) {
        Assert.notNull(registerDTO, "注册参数不能为空");

        // 验证密码与确认密码是否一致
        String password = registerDTO.getPassword();
        String confirmPassword = registerDTO.getConfirmPassword();
        if (StrUtil.isBlank(password) || StrUtil.isBlank(confirmPassword)) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_PARAM_ERROR);
        }
        if (!password.equals(confirmPassword)) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_PASSWORD_NOT_SAME);
        }

        // 验证码校验
        String email = registerDTO.getEmail();
        String code = registerDTO.getCode();
        String redisKey = RedisConstants.REDIS_EMAIL_REGISTER_CODE_KEY + email;
        // 从 Redis 获取验证码
        String cacheCode = redisUtil.getStr(redisKey);
        // 校验验证码
        if (cacheCode == null) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_CODE_EXPIRED);
        }
        if (!cacheCode.equals(code)) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_CODE_ERROR);
        }

        // 邮箱查重
        long count = userService.count(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (count > 0) {
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_USER_EXIST);
        }

        User user = userConvert.registerDtoToEntity(registerDTO);

        // 设置默认头像
        user.setAvatar(GravatarUtils.getRetroAvatar(registerDTO.getEmail()));
        // 设置默认角色
        user.setRole(BizStatus.Role.USER);
        // 设置默认状态
        user.setStatus(BizStatus.User.NORMAL);
        // 设置默认昵称
        user.setNickname(Constants.DEFAULT_NICKNAME_PREFIX + RandomUtil.randomString(8));

        // 密码加密（核心：明文→BCrypt哈希）
        String encryptedPassword = PasswordEncoderUtil.encode(password);
        user.setPassword(encryptedPassword);

        // 执行插入（利用数据库唯一索引保证邮箱不重复）
        try {
            userService.save(user);
        } catch (DuplicateKeyException e) {
            // 捕获并发注册导致的冲突，或者已存在的邮箱
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_USER_EXIST);
        }

        // 将 Redis 清理和事件发布注册到事务提交后的钩子中
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // 只有数据库真正 commit 成功后，才删除 Redis 验证码和发通知
                    // 防止数据库回滚了，验证码却没了，导致用户无法重试
                    redisUtil.delete(redisKey);
                    eventPublisher.publishEvent(new UserRegisteredEvent(this, user.getId()));
                }
            });
        } else {
            // 兜底逻辑（防脱离事务环境运行）
            redisUtil.delete(redisKey);
            eventPublisher.publishEvent(new UserRegisteredEvent(this, user.getId()));
        }

        // 构建 Token 并返回 (这部分由于写入的是新 Token，放在这里没问题)
        return buildLoginResult(user, false, Constants.LOG_REGISTER_AND_LOGIN_SUCCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPasswordByEmail(UserForgotPwdDTO forgotPwdDTO) {
        Assert.notNull(forgotPwdDTO, "重置密码参数不能为空");

        String email = forgotPwdDTO.getEmail();

        // 1. 获取并校验验证码
        String redisKey = RedisConstants.REDIS_EMAIL_RESET_CODE_KEY + email;
        String cacheCode = redisUtil.getStr(redisKey);

        if (cacheCode == null) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_CODE_EXPIRED);
        }
        if (!cacheCode.equals(forgotPwdDTO.getCode())) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_CODE_ERROR);
        }

        // 2. 查询用户
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_USER_NOT_EXIST);
        }

        // 3. 更新密码并加密
        user.setPassword(PasswordEncoderUtil.encode(forgotPwdDTO.getNewPassword()));
        userService.updateById(user);

        // 4. 将所有 Redis 清理操作延迟到事务提交后执行
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    executeResetPasswordCacheClear(redisKey, user.getId());
                }
            });
        } else {
            executeResetPasswordCacheClear(redisKey, user.getId());
        }
    }

    @Override
    public void logout(String token) {
        if (StrUtil.isBlank(token)) {
            return;
        }

        try {
            // 1. 手动解码 JWT，不仅验证格式，还要提取 userId
            DecodedJWT jwt;
            try {
                jwt = JWT.decode(token);
            } catch (Exception e) {
                throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_TOKEN_FAKE);
            }

            // 2. 将当前传入的 Token 拉入黑名单
            this.blacklistToken(token);

            // 3. 从 JWT 载荷中提取出真实的用户 ID 去删缓存
            Long userId = jwt.getClaim("userId").asLong();
            if (userId != null) {
                redisUtil.delete(RedisConstants.REDIS_USER_INFO_KEY + userId);
                redisUtil.delete(RedisConstants.REDIS_USER_TOKEN_KEY + userId);
            }

        } finally {
            // 4. 清理当前线程变量
            UserContext.remove();
        }
    }

    @Override
    public void forceLogoutByUserId(Long userId) {
        if (userId == null) {
            return;
        }

        // 1. 从 Redis 中找出该用户当前在线的 Token，并将其拉入黑名单
        String tokenKey = RedisConstants.REDIS_USER_TOKEN_KEY + userId;
        Object oldTokenObj = redisUtil.get(tokenKey);

        if (oldTokenObj != null) {
            this.blacklistToken(oldTokenObj.toString());
            // 彻底移除该用户的在线 Token 记录
            redisUtil.delete(tokenKey);
        }

        // 2. 清除用户的基本信息缓存
        redisUtil.delete(RedisConstants.REDIS_USER_INFO_KEY + userId);
    }
}