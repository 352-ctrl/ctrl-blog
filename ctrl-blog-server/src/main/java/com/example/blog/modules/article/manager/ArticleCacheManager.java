package com.example.blog.modules.article.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.modules.article.model.dto.ArticleVisitorDTO;
import com.example.blog.modules.article.model.entity.Article;
import com.example.blog.modules.user.event.UserInfoChangedEvent;
import com.example.blog.modules.article.mapper.ArticleMapper;
import com.example.blog.common.utils.RedisUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文章缓存与底层组件管理器
 * 专门封装 Redis 相关的底层操作，如缓存清理、浏览量实时同步、事件监听等
 */
@Slf4j
@Component
public class ArticleCacheManager {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ArticleMapper articleMapper;

    /**
     * 清理文章详情缓存
     *
     * @param articleId 文章ID
     */
    public void clearArticleDetailCache(Long articleId) {
        if (articleId != null) {
            redisUtil.delete(RedisConstants.REDIS_ARTICLE_DETAIL_PREFIX + articleId);
        }
    }

    /**
     * 清理全局文章相关缓存（当文章新增、更新、删除时调用）
     * 包含了列表、归档、轮播图、热门文章、分类、标签以及站长信息
     */
    public void clearGlobalArticleCache() {
        // 1. 清理原有的列表和归档缓存
        clearListCache();

        // 2. 清理轮播图和热门文章
        redisUtil.delete(RedisConstants.REDIS_ARTICLE_CAROUSEL_KEY);
        redisUtil.delete(RedisConstants.REDIS_ARTICLE_HOT_KEY);

        // 3. 清理分类和标签（因为文章数量变更会影响元数据统计）
        redisUtil.delete(RedisConstants.REDIS_CATEGORY_LIST_KEY);
        redisUtil.delete(RedisConstants.REDIS_TAG_LIST_KEY);

        // 4. 清理站长信息（影响文章总数统计）
        redisUtil.delete(RedisConstants.REDIS_WEBMASTER_INFO_KEY);
    }

    /**
     * 私有辅助方法：清理列表相关的缓存
     * (包含：归档数据、首页第一页数据)
     */
    public void clearListCache() {
        // 删除归档缓存
        redisUtil.delete(RedisConstants.REDIS_ARTICLE_ARCHIVE_KEY);
        // 删除首页第一页缓存
        redisUtil.delete(RedisConstants.REDIS_ARTICLE_LIST_FIRST_PAGE_KEY);
    }

    /**
     * 通用的浏览量同步方法 (利用 Java 8 函数式接口)
     *
     * @param list            需要同步的列表 (Entity 或各种 VO 均可)
     * @param idExtractor      提取 ID 的方法引用 (例如：Article::getId)
     * @param viewCountSetter 设置浏览量的方法引用 (例如：Article::setViewCount)
     * @param <T>              对象的泛型类型
     */
    public <T> void syncViewCount(List<T> list, Function<T, Long> idExtractor, BiConsumer<T, Long> viewCountSetter) {
        if (CollUtil.isEmpty(list)) {
            return;
        }

        // 1. 动态提取 ID 列表
        List<Object> articleIds = list.stream()
                .map(idExtractor)       // 动态调用 getId()
                .map(String::valueOf)
                .collect(Collectors.toList());

        // 2. 从 Redis 批量获取实时阅读量
        List<Object> viewCounts = redisUtil.hMultiGet(RedisConstants.REDIS_VIEW_HASH_KEY, articleIds);

        // 3. 遍历覆盖旧值
        for (int i = 0; i < list.size(); i++) {
            Object countObj = viewCounts.get(i);
            if (ObjectUtil.isNotNull(countObj)) {
                try {
                    Long realTimeCount = Long.valueOf(countObj.toString());
                    // 动态调用 setViewCount()
                    viewCountSetter.accept(list.get(i), realTimeCount);
                } catch (NumberFormatException e) {
                    // 忽略异常，保持原值
                    log.warn("同步浏览量转换异常，保持原值: {}", countObj);
                }
            }
        }
    }

    /**
     * 监听用户信息变更事件，清除相关的文章缓存
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserInfoChanged(UserInfoChangedEvent event) {
        log.info("接收到用户信息变更事件，开始清理相关文章缓存，用户ID: {}", event.getUserId());
        // 清除公共文章列表缓存
        redisUtil.delete(RedisConstants.REDIS_ARTICLE_LIST_FIRST_PAGE_KEY);
        redisUtil.delete(RedisConstants.REDIS_ARTICLE_HOT_KEY);
        redisUtil.delete(RedisConstants.REDIS_ARTICLE_CAROUSEL_KEY);

        // 清除该用户的文章详情缓存
        List<Article> userArticles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .select(Article::getId)
                        .eq(Article::getUserId, event.getUserId())
        );

        if (CollUtil.isNotEmpty(userArticles)) {
            List<String> articleCacheKeys = userArticles.stream()
                    .map(article -> RedisConstants.REDIS_ARTICLE_DETAIL_PREFIX + article.getId())
                    .collect(Collectors.toList());
            redisUtil.delete(articleCacheKeys);
        }
    }

    /**
     * 增加文章阅读量 (基于 Redis 缓存与防刷机制)
     */
    public void incrementViewCount(ArticleVisitorDTO visitorDTO) {
        if (visitorDTO.getArticleId() == null || StrUtil.isBlank(visitorDTO.getIp())) {
            return;
        }

        Long articleId = visitorDTO.getArticleId();
        String ip = visitorDTO.getIp();
        String userAgent = visitorDTO.getUserAgent();

        // 生成防刷指纹 (使用 Hutool 的 SecureUtil)
        String identity = ip + (StrUtil.isNotBlank(userAgent) ? userAgent : StrUtil.EMPTY);
        String visitorFingerprint = SecureUtil.md5(identity);

        // 定义防刷 Key (格式: blog:view:limit:文章ID:IP)
        String limitKey = RedisConstants.REDIS_VIEW_LIMIT_PREFIX + articleId + ":" + visitorFingerprint;

        // 检查该 IP 是否在 1 分钟内访问过
        // 如果 Key 存在，说明还在限制时间内，直接返回，不增加阅读量
        if (redisUtil.hasKey(limitKey)) {
            return;
        }

        // 标记该访客已访问，1分钟过期
        redisUtil.set(limitKey, RedisConstants.VIEW_LIMIT_VALUE, RedisConstants.VIEW_LIMIT_EXPIRE, TimeUnit.MINUTES);

        try {
            String hashKey = RedisConstants.REDIS_VIEW_HASH_KEY;
            String field = articleId.toString();

            // 1. 直接自增
            long currentCount = redisUtil.hIncr(hashKey, field, 1L);

            // 2. 如果自增后结果为 1，说明 Redis 之前没这文章的数据，需要从 DB 补齐
            if (currentCount == 1) {
                Article article = articleMapper.selectById(articleId);
                long dbCount = (article != null && article.getViewCount() != null) ? article.getViewCount() : 0L;
                // 补偿：DB 值 + 1（因为刚才那次访问也是有效的）
                redisUtil.hSet(hashKey, field, dbCount + 1);
            }

            // 3. 记录脏数据
            redisUtil.sSet(RedisConstants.REDIS_VIEW_DIRTY_SET, articleId);
        } catch (Exception e) {
            log.error("Redis 计数异常", e);
        }
    }
}