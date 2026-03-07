package com.example.blog.core.security;

import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.blog.common.constants.Constants;
import com.example.blog.modules.user.model.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT令牌工具类
 * 提供Token生成、解析和当前用户获取功能
 */
@Slf4j
@Component
public class TokenUtils {

    // 1. 定义非静态变量接收配置
    @Value("${blog.jwt.secret}")
    private String secret;

    @Value("${blog.jwt.expire-day}")
    private int expireDay;

    private static String SECRET;
    private static int EXPIRE_DAY;

    @PostConstruct
    public void init() {
        // 将配置赋值给静态变量
        SECRET = this.secret;
        EXPIRE_DAY = this.expireDay;
    }

    /**
     * 生成JWT令牌
     *
     * @param user 用户实体
     * @return JWT令牌字符串
     */
    public static String getToken(User user) {
        return JWT.create()
                // 1. 存入标准 Payload
                .withAudience(user.getId().toString()) // 接收方(用户ID)
                .withExpiresAt(DateUtil.offsetDay(new Date(), EXPIRE_DAY)) // 过期时间

                // 2. 存入自定义 Payload
                .withClaim(Constants.CLAIM_ID, user.getId())
                .withClaim(Constants.CLAIM_ROLE, user.getRole().getValue())
                .withClaim(Constants.CLAIM_NICKNAME, user.getNickname())


                // 3. 签名
                .sign(Algorithm.HMAC256(SECRET));
    }

    /**
     * 校验 Token 是否正确 (通常在拦截器中使用)
     * @param token 前端传来的token
     * @return true=有效
     */
    public static DecodedJWT verify(String token) {
        // 返回 DecodedJWT 对象，包含了用户信息
        return JWT.require(Algorithm.HMAC256(SECRET)).build().verify(token);
    }
}