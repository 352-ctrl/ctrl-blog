package com.example.blog.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.dto.article.ArticleVisitorDTO;
import com.example.blog.entity.Article;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.utils.RedisUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文章通用管理器 (Manager层)
 * 职责：负责封装 Redis 缓存控制、浏览量防刷与同步等偏底层的通用逻辑。
 * 架构规范：Controller -> Service -> Manager -> Mapper
 */
@Slf4j
@Component
public class ArticleManager {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ArticleMapper articleMapper;

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
     * @param idExtractor     提取 ID 的方法引用 (例如：Article::getId)
     * @param viewCountSetter 设置浏览量的方法引用 (例如：Article::setViewCount)
     * @param <T>             对象的泛型类型
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
     * 增加文章阅读量 (基于 Redis 缓存与防刷机制)
     * <p>
     * 核心处理逻辑：
     * 1. 【防刷拦截】：根据访客的 IP 和 UserAgent 生成唯一 MD5 指纹，利用 Redis 键的过期机制实现 1 分钟内同一用户的访问仅记录一次。
     * 2. 【实时计数】：通过 Redis Hash 结构 (hIncr) 实时对对应文章的阅读量进行原子自增。如果 Redis 中暂无记录，则先从数据库回源初始化。
     * 3. 【异步标记】：将被访问过的文章 ID 存入 Redis Set 集合作为“脏数据”标记，等待后台定时任务统一步伐持久化到 MySQL，大幅降低数据库写压力。
     * </p>
     *
     * @param visitorDTO 访客请求信息对象，必须包含文章ID (articleId) 与 IP地址 (ip)
     */
    public void incrementViewCount(ArticleVisitorDTO visitorDTO) {
        Assert.notNull(visitorDTO, "访问记录参数不能为空");

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
                // 直接调用 mapper 进行兜底查询
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