package com.example.blog.core.aspect;

import cn.hutool.core.util.StrUtil;
import com.example.blog.core.annotation.RateLimit;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.core.exception.CustomerException;
import com.example.blog.common.utils.IpUtils;
import com.example.blog.common.utils.RedisUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class RateLimitAspect {

    @Resource
    private RedisUtil redisUtil;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        // 1. 获取 Request 和 IP
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = IpUtils.getIpAddr(request);

        // 2. 拼接 Redis Key: rate_limit:IP:MethodName
        String key = RedisConstants.REDIS_RATE_LIMIT_KEY + ip + StrUtil.COLON + point.getSignature().toShortString();

        // 3. 结合 Redis 计数
        long count = redisUtil.increment(key, 1);

        // 第一次访问，设置过期时间
        if (count == 1) {
            redisUtil.expire(key, rateLimit.time(), TimeUnit.SECONDS);
        }

        // 4. 超出限制，抛出异常
        if (count > rateLimit.count()) {
            throw new CustomerException(ResultCode.TOO_MANY_REQUESTS, MessageConstants.MSG_SEND_FREQUENTLY);
        }

        return point.proceed();
    }
}
