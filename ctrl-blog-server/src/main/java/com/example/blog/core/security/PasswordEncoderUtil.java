package com.example.blog.core.security;

import org.mapstruct.Named;
import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * 密码加密工具类（基于BCrypt）
 */
public class PasswordEncoderUtil {

    /**
     * 生成BCrypt哈希密码（自动生成盐，工作因子默认10）
     * @param rawPassword 明文密码
     * @return 加密后的哈希字符串（包含盐和工作因子）
     */
    @Named("encode")
    public static String encode(String rawPassword) {
        // 工作因子（4-31）：值越大，加密越慢，安全性越高，推荐10-12
        int workload = 10;
        // 生成盐（自动随机）
        String salt = BCrypt.gensalt(workload);
        // 加密密码（哈希结果包含：$2a$10$[盐]$[哈希值]）
        return BCrypt.hashpw(rawPassword, salt);
    }

    /**
     * 验证明文密码是否匹配哈希密码
     * @param rawPassword 明文密码
     * @param hashedPassword 加密后的哈希密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }

}
