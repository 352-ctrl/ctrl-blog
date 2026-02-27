package com.example.blog.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.convert.UserConvert;
import com.example.blog.dto.EmailRequestDTO;
import com.example.blog.dto.message.MessageSendDTO;
import com.example.blog.dto.user.UserForgotPwdDTO;
import com.example.blog.dto.user.UserLoginDTO;
import com.example.blog.dto.user.UserPayloadDTO;
import com.example.blog.dto.user.UserRegisterDTO;
import com.example.blog.entity.User;
import com.example.blog.exception.CustomerException;
import com.example.blog.service.AuthService;
import com.example.blog.service.SysLoginLogService;
import com.example.blog.service.SysMessageService;
import com.example.blog.service.UserService;
import com.example.blog.utils.*;
import com.example.blog.vo.UserLoginVO;
import com.example.blog.vo.UserVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
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
    private SysLoginLogService sysLoginLogService;

    @Resource
    private SysMessageService sysMessageService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private CaptchaService captchaService;

    /**
     * 校验滑动验证码二次凭证
     * @param captchaVerification 前端传入的加密凭证
     */
    private void verifyCaptcha(String captchaVerification) {
        if (StrUtil.isBlank(captchaVerification)) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_CAPTCHA_REQUIRE);
        }

        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaVerification(captchaVerification);
        ResponseModel response = captchaService.verification(captchaVO);

        if (!response.isSuccess()) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_CAPTCHA_VERIFY_FAILED);
        }
    }

    /**
     * 统一获取网络信息并记录认证日志
     */
    private void recordAuthLog(String email, Integer status, String msg) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        String ip = IpUtils.getIpAddr(request);
        String userAgent = request != null ? request.getHeader(Constants.HEADER_USER_AGENT) : "";

        sysLoginLogService.recordLoginLog(email, status, msg, ip, userAgent);
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
    private void doSendEmailCode(String captchaVerification, String email, String redisKeyPrefix, String subject) {
        // 1. 滑动验证码二次校验
        verifyCaptcha(captchaVerification);

        // 2. 防刷校验
        String redisKey = redisKeyPrefix + email;
        long expire = java.util.Optional.ofNullable(redisUtil.getExpire(redisKey, TimeUnit.SECONDS)).orElse(-2L);

        // 当Key不存在时，expire 返回 -2。如果 > 240 说明刚发过不到1分钟
        if (expire > 240) {
            throw new CustomerException(ResultCode.FORBIDDEN, MessageConstants.MSG_SEND_FREQUENTLY);
        }

        // 3. 生成 6 位数字验证码
        String code = RandomUtil.randomNumbers(6);

        // 4. 存入 Redis，有效期 5 分钟
        redisUtil.setStr(redisKey, code, RedisConstants.EXPIRE_EMAIL_CODE, TimeUnit.MINUTES);

        // 5. 封装参数并异步发送邮件
        Map<String, Object> model = new HashMap<>();
        model.put("code", code);
        model.put("title", subject);
        mailService.sendHtmlMail(email, subject, model);
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
                emailRequestDTO.getCaptchaVerification(),
                email,
                RedisConstants.REDIS_EMAIL_REGISTER_CODE_KEY,
                MessageConstants.MSG_EMAIL_SUBJECT_REGISTER
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
                emailRequestDTO.getCaptchaVerification(),
                email,
                RedisConstants.REDIS_EMAIL_RESET_CODE_KEY,
                MessageConstants.MSG_EMAIL_SUBJECT_RESET
        );
    }

    @Override
    public UserLoginVO login(UserLoginDTO loginDTO) {
        Assert.notNull(loginDTO, "登录参数不能为空");

        // 滑动验证码二次校验
        verifyCaptcha(loginDTO.getCaptchaVerification());

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
                recordAuthLog(email, BizStatus.Log.FAIL.getValue(), MessageConstants.LOG_LOGIN_LOCKED);
                // 抛出带有动态时间的异常
                throw new CustomerException(ResultCode.FORBIDDEN, msg);
            }
        }
        // 查询用户
        User dbUser = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, loginDTO.getEmail()));
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
                recordAuthLog(email, BizStatus.Log.FAIL.getValue(), MessageConstants.LOG_LOGIN_LOCKED);
                // 锁定 30 分钟
                throw new CustomerException(ResultCode.FORBIDDEN, String.format(MessageConstants.MSG_LOGIN_LOCKED, RedisConstants.LOGIN_LOCKED_TIME));
            } else {
                // 还没到 5 次，抛出普通的密码错误
                recordAuthLog(email, BizStatus.Log.FAIL.getValue(), MessageConstants.LOG_LOGIN_PWD_ERROR);
                throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_LOGIN_ERROR);
            }
        }

        if (dbUser.getStatus() == BizStatus.User.DISABLE) {
            recordAuthLog(email, BizStatus.Log.FAIL.getValue(), MessageConstants.LOG_LOGIN_BANNED);
            // 如果是封禁状态，即使密码对也不让进
            throw new CustomerException(ResultCode.FORBIDDEN, MessageConstants.MSG_ACCOUNT_BANNED);
        }

        boolean isRestored = false; // 标记是否触发了恢复
        if (dbUser.getStatus() == BizStatus.User.COOL_OFF) {
            dbUser.setStatus(BizStatus.User.NORMAL);
            userService.updateById(dbUser);
            isRestored = true; // 标记为已恢复
        }

        // 登录成功，清除失败记录
        redisUtil.delete(failKey);

        // 调用私有方法统一构建返回值
        return buildLoginResult(dbUser, isRestored, MessageConstants.LOG_LOGIN_SUCCESS);
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

        // 注册成功后，删除 Redis 中的验证码
        redisUtil.delete(redisKey);

        // 发送欢迎系统通知
        sysMessageService.sendSystemNotice(
                MessageSendDTO.builder()
                        .toUserId(user.getId())
                        .title(MessageConstants.TITLE_WELCOME)
                        .content(MessageConstants.CONTENT_WELCOME)
                        .build()
        );
        // 调用私有方法统一构建返回值
        return buildLoginResult(user, false, MessageConstants.LOG_REGISTER_AND_LOGIN_SUCCESS);
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

        // 4. 删除验证码缓存
        redisUtil.delete(redisKey);

        // 5. 踢出该用户，强制重新登录 (核心：Token 失效逻辑)
        // 5.1 清除用户的基本信息缓存
        redisUtil.delete(RedisConstants.REDIS_USER_INFO_KEY + user.getId());

        // 5.2 获取该用户当前关联的 Token，并将其拉入黑名单
        String tokenKey = RedisConstants.REDIS_USER_TOKEN_KEY + user.getId();
        Object oldTokenObj = redisUtil.get(tokenKey);

        if (oldTokenObj != null) {
            String oldToken = oldTokenObj.toString();
            try {
                // 解析旧 Token 的过期时间
                DecodedJWT jwt = JWT.decode(oldToken);
                long remainingTime = jwt.getExpiresAt().getTime() - System.currentTimeMillis();

                // 如果旧 Token 尚未过期，将其加入黑名单
                if (remainingTime > 0) {
                    String blacklistKey = RedisConstants.REDIS_TOKEN_BLACKLIST_PREFIX + oldToken;
                    redisUtil.set(blacklistKey, RedisConstants.REDIS_TOKEN_BLACKLIST_VALUE, remainingTime, TimeUnit.MILLISECONDS);
                }
            } catch (Exception e) {
                // Token 解析失败说明格式不合法或已损坏，忽略即可
            }
            // 5.3 从 Redis 中彻底移除该用户的在线 Token 记录
            redisUtil.delete(tokenKey);
        }
    }

    @Override
    public void logout(String token) {
        // 1. 基础校验
        if (StrUtil.isBlank(token)) {
            return;
        }

        try {
            // 2. 解析 Token 获取过期时间
            DecodedJWT jwt = JWT.decode(token);
            Date expiresAt = jwt.getExpiresAt();

            // 3. 计算 Token 距离过期的剩余毫秒数
            long currentTime = System.currentTimeMillis();
            long remainingTime = expiresAt.getTime() - currentTime;

            // 4. 如果 Token 尚未过期，则将其拉入黑名单
            if (remainingTime > 0) {
                String blacklistKey = RedisConstants.REDIS_TOKEN_BLACKLIST_PREFIX + token;

                // 将 Token 存入 Redis，过期时间设为 Token 的剩余寿命
                // 这样可以保证 Redis 空间不被永久浪费，Token 真正过期后黑名单也会自动消失
                redisUtil.set(blacklistKey, RedisConstants.REDIS_TOKEN_BLACKLIST_VALUE, remainingTime, TimeUnit.MILLISECONDS);
            }

            // 5. 清理该用户的基本信息缓存 (如果有的话)
            UserPayloadDTO currentUser = UserContext.get();
            if (currentUser != null && currentUser.getId() != null) {
                Long userId = currentUser.getId();
                // 5.1 清理用户信息缓存
                redisUtil.delete(RedisConstants.REDIS_USER_INFO_KEY + userId);
                // 5.2 清理该用户的在线 Token 记录，保持 Redis 干净
                redisUtil.delete(RedisConstants.REDIS_USER_TOKEN_KEY + userId);
            }

        } catch (Exception e) {
            // 解析失败说明 Token 本身无效，无需额外处理
        } finally {
            // 6. 强制清理当前线程变量
            UserContext.remove();
        }
    }
}