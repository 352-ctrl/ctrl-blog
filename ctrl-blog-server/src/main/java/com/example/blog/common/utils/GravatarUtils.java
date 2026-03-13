package com.example.blog.common.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import org.apache.commons.lang3.StringUtils;

public class GravatarUtils {

    // 使用国内速度最快的 Cravatar 镜像源
    private static final String GRAVATAR_BASE_URL = "https://cn.cravatar.com/avatar/";

    // 指定默认风格为复古像素风 (retro)，并设置图片尺寸为 256x256, 加上 &f=y 强制显示默认像素图
    private static final String GRAVATAR_PARAMS = "?d=retro&s=256&f=y";

    /**
     * 根据邮箱生成 Gravatar 头像 URL (Retro 风格)
     *
     * @param email 用户的邮箱地址
     * @return 头像的完整 URL 链接
     */
    public static String getRetroAvatar(String email) {
        // 1. 如果邮箱为空，生成一个随机的 32 位字符串（UUID去横杠），确保得到不同的随机像素头像
        if (StringUtils.isBlank(email)) {
            return GRAVATAR_BASE_URL + IdUtil.simpleUUID() + GRAVATAR_PARAMS;
        }

        // 2. Gravatar 要求：去除首尾空格，并全部转换为小写
        String cleanEmail = email.trim().toLowerCase();

        // 3. 使用你 pom.xml 中引入的 Hutool 进行 MD5 加密
        String emailMd5 = DigestUtil.md5Hex(cleanEmail);

        // 4. 拼接并返回完整 URL
        return GRAVATAR_BASE_URL + emailMd5 + GRAVATAR_PARAMS;
    }
}