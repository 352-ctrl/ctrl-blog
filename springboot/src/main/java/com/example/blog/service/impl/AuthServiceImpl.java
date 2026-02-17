package com.example.blog.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
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
import com.example.blog.dto.user.UserLoginDTO;
import com.example.blog.dto.user.UserRegisterDTO;
import com.example.blog.entity.User;
import com.example.blog.exception.CustomerException;
import com.example.blog.service.AuthService;
import com.example.blog.service.UserService;
import com.example.blog.utils.*;
import com.example.blog.vo.UserLoginVO;
import com.example.blog.vo.UserVO;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private RedisUtil redisUtil;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void sendEmailCode(EmailRequestDTO emailRequestDTO) {
        String email = emailRequestDTO.getEmail();
        // 1. 校验邮箱是否已注册
        long count = userService.count(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (count > 0) {
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_EMAIL_EXIST);
        }

        // 2. 防刷校验：检查Redis里是否还有未过期的验证码，防止频繁发送
        String redisKey = RedisConstants.REDIS_EMAIL_CODE_KEY + email;
        Long expire = stringRedisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        // 如果有效期还剩超过 4分30秒 (说明刚发过不到30秒)，拦截
        if (expire != null && expire > 270) {
            throw new CustomerException(ResultCode.FORBIDDEN, MessageConstants.MSG_SEND_FREQUENTLY);
        }

        // 3. 生成 6 位数字验证码 (使用 Hutool 工具)
        String code = RandomUtil.randomNumbers(6);

        // 4. 存入 Redis，有效期 5 分钟
        stringRedisTemplate.opsForValue().set(redisKey, code, RedisConstants.EXPIRE_EMAIL_CODE, TimeUnit.MINUTES);

        // 5. 封装 FreeMarker 模板所需的参数
        Map<String, Object> model = new HashMap<>();
        model.put("code", code); // 对应模板中的 ${code}

        // 6. 调用异步 HTML 邮件发送方法
        mailService.sendHtmlMail(email, MessageConstants.MSG_EMAIL_SUBJECT_REGISTER, model);
    }

    @Override
    public UserLoginVO login(UserLoginDTO loginDTO) {
        String email = loginDTO.getEmail();

        // 检查是否被锁定
        String failKey = RedisConstants.REDIS_LOGIN_FAIL_KEY + email;
        if (redisUtil.hasKey(failKey)) {
            int failCount = Convert.toInt(redisUtil.get(failKey), 0);
            if (failCount >= 5) {
                // 获取剩余锁定时间，给前端展示“请xx分钟后再试”
                Long expire = redisUtil.getExpire(failKey, TimeUnit.MINUTES);
                // 防御性处理：万一获取失败或者刚好过期，给个默认值 1
                long displayTime = (expire != null && expire > 0) ? (expire + 1) : 1;
                // 格式化字符串
                String msg = String.format(MessageConstants.MSG_LOGIN_LOCKED, displayTime);
                // 抛出带有动态时间的异常
                throw new CustomerException(ResultCode.FORBIDDEN, msg);
            }
        }
        // 查询用户
        User dbUser = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, loginDTO.getEmail()));
        if (dbUser == null) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_LOGIN_ERROR);
        }

        // 验证密码
        if (!PasswordEncoderUtil.matches(loginDTO.getPassword(), dbUser.getPassword())) {
            // 记录失败次数
            recordLoginFailed(failKey);
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_LOGIN_ERROR);
        }

        if (dbUser.getStatus() == BizStatus.User.DISABLE) {
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

        // 创建token
        String token = TokenUtils.getToken(dbUser);

        // 将 Token 存入 Redis，用于踢人或退出登录 (有效期 1 天)
        redisUtil.set(RedisConstants.REDIS_USER_TOKEN_KEY + dbUser.getId(), token, RedisConstants.EXPIRE_USER_TOKEN, TimeUnit.DAYS);

        UserVO userVO = userConvert.entityToVo(dbUser);
        userVO.setCreateTime(null);

        return UserLoginVO.builder()
                .token(token)
                .userInfo(userVO)
                .isRestored(isRestored)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterDTO registerDTO) {
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
        String redisKey = RedisConstants.REDIS_EMAIL_CODE_KEY + email;
        // 从 Redis 获取验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(redisKey);
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

        // 如果没有昵称，设置默认昵称 "用户xxxxxx"
        if (StrUtil.isBlank(user.getNickname())) {
            user.setNickname(Constants.DEFAULT_NICKNAME_PREFIX + RandomUtil.randomString(6));
        }
        // 如果没有头像，设置默认头像
        if (StrUtil.isBlank(user.getAvatar())) {
            user.setAvatar(Constants.DEFAULT_AVATAR);
        }
        // 设置默认角色
        user.setRole(BizStatus.Role.USER);
        // 设置默认状态
        user.setStatus(BizStatus.User.NORMAL);

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
        stringRedisTemplate.delete(redisKey);
    }

    /**
     * 记录登录失败次数
     */
    private void recordLoginFailed(String key) {
        // 如果 key 不存在，redisUtil.increment 会自动创建并设为1
        long count = redisUtil.increment(key, 1);
        if (count == 1) {
            // 第一次失败，设置过期时间 30 分钟
            redisUtil.expire(key, RedisConstants.EXPIRE_LOGIN_FAIL_WINDOW, TimeUnit.MINUTES);
        }
        // 如果达到了锁定阈值，强制设置一个锁定时间
        if (count >= 5) {
            // 覆盖过期时间，强制锁定 30 分钟
            redisUtil.expire(key, RedisConstants.LOGIN_LOCKED_TIME, TimeUnit.MINUTES);
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
                // Key 示例: token:blacklist:ey... (完整的token字符串)
                String blacklistKey = RedisConstants.REDIS_TOKEN_BLACKLIST_PREFIX + token;

                // 将 Token 存入 Redis，过期时间设为 Token 的剩余寿命
                // 这样可以保证 Redis 空间不被永久浪费，Token 真正过期后黑名单也会自动消失
                redisUtil.set(blacklistKey, RedisConstants.REDIS_TOKEN_BLACKLIST_VALUE, remainingTime, TimeUnit.MILLISECONDS);
            }

            // 5. 清理该用户的基本信息缓存 (如果有的话)
            Long userId = UserContext.get().getId();
            if (userId != null) {
                redisUtil.delete(RedisConstants.REDIS_USER_INFO_KEY + userId);
            }

        } catch (Exception e) {
            // 解析失败说明 Token 本身无效，无需额外处理
        } finally {
            // 6. 强制清理当前线程变量
            UserContext.remove();
        }
    }
}