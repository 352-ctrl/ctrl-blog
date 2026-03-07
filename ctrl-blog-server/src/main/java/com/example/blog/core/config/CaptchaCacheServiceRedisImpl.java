package com.example.blog.core.config;

import com.anji.captcha.service.CaptchaCacheService;
import com.example.blog.common.utils.RedisUtil;
import com.example.blog.common.utils.SpringContextUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * AJ-Captcha 滑动验证码的 Redis 缓存实现
 */
@Service
public class CaptchaCacheServiceRedisImpl implements CaptchaCacheService {

    /**
     * 由于该类被 Java SPI 机制加载，@Resource 无法生效。
     * 改为使用 SpringContextUtils 动态获取 RedisUtil 实例。
     */
    private RedisUtil getRedisUtil() {
        return SpringContextUtils.getBean(RedisUtil.class);
    }

    @Override
    public void set(String key, String value, long expiresInSeconds) {
        getRedisUtil().set(key, value, expiresInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean exists(String key) {
        return getRedisUtil().hasKey(key);
    }

    @Override
    public void delete(String key) {
        getRedisUtil().delete(key);
    }

    @Override
    public String get(String key) {
        Object val = getRedisUtil().get(key);
        return val != null ? val.toString() : null;
    }

    @Override
    public String type() {
        return "redis";
    }
}