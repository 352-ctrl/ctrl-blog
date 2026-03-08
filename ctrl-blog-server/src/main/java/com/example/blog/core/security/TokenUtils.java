package com.example.blog.core.security;

import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.user.model.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT令牌工具类
 * 提供Token生成、解析功能
 */
@Slf4j
@Component
public class TokenUtils {

    @Value("${blog.jwt.secret}")
    private String secret;

    // 注入不同角色的过期时间配置
    @Value("${blog.jwt.expire.user-days:7}")
    private int userExpireDays;

    @Value("${blog.jwt.expire.admin-hours:12}")
    private int adminExpireHours;

    @Value("${blog.jwt.expire.super-admin-hours:2}")
    private int superAdminExpireHours;

    private static String SECRET;
    private static int USER_EXPIRE_DAYS;
    private static int ADMIN_EXPIRE_HOURS;
    private static int SUPER_ADMIN_EXPIRE_HOURS;

    @PostConstruct
    public void init() {
        // 将配置赋值给静态变量，方便静态方法调用
        SECRET = this.secret;
        USER_EXPIRE_DAYS = this.userExpireDays;
        ADMIN_EXPIRE_HOURS = this.adminExpireHours;
        SUPER_ADMIN_EXPIRE_HOURS = this.superAdminExpireHours;
    }

    /**
     * 生成 JWT 令牌
     *
     * @param user 用户实体
     * @return JWT令牌字符串
     */
    public static String getToken(User user) {
        // 1. 根据角色计算具体的过期时间点
        Date expireDate = calculateExpireDate(user.getRole());

        // 2. 构建 JWT
        return JWT.create()
                // 1. 存入标准 Payload
                .withAudience(user.getId().toString()) // 接收方(用户ID)
                .withExpiresAt(expireDate) // 过期时间

                // 2. 存入自定义 Payload
                .withClaim(Constants.CLAIM_ID, user.getId())
                .withClaim(Constants.CLAIM_ROLE, user.getRole().getValue())
                .withClaim(Constants.CLAIM_NICKNAME, user.getNickname())
                // 3. HMAC256 签名
                .sign(Algorithm.HMAC256(SECRET));
    }

    /**
     * 根据角色计算过期时间
     */
    private static Date calculateExpireDate(BizStatus.Role role) {
        Date now = new Date();
        if (role == null) {
            return DateUtil.offsetDay(now, USER_EXPIRE_DAYS); // 默认给普通用户时长
        }

        return switch (role) {
            case SUPER_ADMIN -> DateUtil.offsetHour(now, SUPER_ADMIN_EXPIRE_HOURS);
            case ADMIN -> DateUtil.offsetHour(now, ADMIN_EXPIRE_HOURS);
            default -> DateUtil.offsetDay(now, USER_EXPIRE_DAYS);
        };
    }

    /**
     * 校验 Token 是否合法且未篡改
     */
    public static DecodedJWT verify(String token) {
        // 验证失败会抛出 JWTVerificationException
        return JWT.require(Algorithm.HMAC256(SECRET)).build().verify(token);
    }
}