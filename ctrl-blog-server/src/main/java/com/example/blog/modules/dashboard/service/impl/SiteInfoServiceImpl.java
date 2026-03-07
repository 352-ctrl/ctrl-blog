package com.example.blog.modules.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.article.model.entity.Article;
import com.example.blog.modules.dashboard.service.SiteInfoService;
import com.example.blog.modules.user.model.entity.User;
import com.example.blog.modules.article.service.ArticleService;
import com.example.blog.modules.article.service.CategoryService;
import com.example.blog.modules.article.service.TagService;
import com.example.blog.modules.user.service.UserService;
import com.example.blog.common.utils.RedisUtil;
import com.example.blog.modules.user.model.vo.WebmasterVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SiteInfoServiceImpl implements SiteInfoService {

    @Resource
    private UserService userService;
    @Resource
    private ArticleService articleService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private TagService tagService;
    @Resource
    private RedisUtil redisUtil;

    @Value("${blog.github-url:}")
    private String githubUrl;

    @Override
    public WebmasterVO getWebmasterInfo() {
        // 1. 优先从 Redis 缓存获取
        Object cachedObj = redisUtil.get(RedisConstants.REDIS_WEBMASTER_INFO_KEY);
        if (cachedObj instanceof WebmasterVO) {
            return (WebmasterVO) cachedObj;
        }

        // 2. 缓存未命中，调用 userService 找“站长”（系统中的第一个 SUPER_ADMIN）
        User webmaster = userService.getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getRole, BizStatus.Role.SUPER_ADMIN)
                        .last("LIMIT 1")
        );

        if (webmaster == null) {
            return new WebmasterVO(); // 极端情况兜底，防止前端报错
        }

        // 3. 调用各个 Service 统计全站数据
        // 文章数（只统计已发布，Service 层默认可能带了逻辑删除拦截，更安全）
        long articleCount = articleService.count(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, BizStatus.Article.PUBLISHED)
        );
        long categoryCount = categoryService.count();
        long tagCount = tagService.count();

        // 4. 组装 VO，并注入 Github 链接
        WebmasterVO vo = WebmasterVO.builder()
                .userId(webmaster.getId())
                .nickname(webmaster.getNickname())
                .avatar(webmaster.getAvatar())
                .bio(webmaster.getBio())
                .articleCount(articleCount)
                .categoryCount(categoryCount)
                .tagCount(tagCount)
                .github(githubUrl) // 注入配置项
                .build();

        // 5. 写入 Redis，设置过期时间 1 天
        redisUtil.set(RedisConstants.REDIS_WEBMASTER_INFO_KEY, vo, RedisConstants.EXPIRE_WEBMASTER_INFO, TimeUnit.DAYS);

        return vo;
    }
}