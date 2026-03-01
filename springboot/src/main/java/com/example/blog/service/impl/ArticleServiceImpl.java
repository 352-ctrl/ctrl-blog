package com.example.blog.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.convert.ArticleConvert;
import com.example.blog.dto.article.*;
import com.example.blog.dto.user.UserPayloadDTO;
import com.example.blog.entity.*;
import com.example.blog.event.ArticlesDeletedEvent;
import com.example.blog.event.InteractiveMessageEvent;
import com.example.blog.exception.CustomerException;
import com.example.blog.manager.ArticleCacheManager;
import com.example.blog.manager.ArticleQueryManager;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.service.*;
import com.example.blog.utils.RedisUtil;
import com.example.blog.utils.SensitiveWordManager;
import com.example.blog.utils.UserContext;
import com.example.blog.vo.article.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章业务服务实现类 (文章领域聚合根 - Command侧)
 * 专门负责核心写入逻辑、系统级协调、级联删除与事务控制
 */
@Slf4j
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Resource
    private ArticleConvert articleConvert;

    @Resource
    private ArticleTagService articleTagService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private ArticleLikeService articleLikeService;

    @Resource
    private ArticleFavoriteService articleFavoriteService;

    @Resource
    private CommentService commentService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private SensitiveWordManager sensitiveWordManager;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Resource
    private ArticleCacheManager articleCacheManager;

    @Resource
    private ArticleQueryManager articleQueryManager;

    /* ================= 以下为读操作，统统委托给 QueryManager ================= */

    @Override
    public List<ArticleSimpleVO> listHotArticles() {
        return articleQueryManager.listHotArticles();
    }

    @Override
    public List<ArticleSearchVO> listSearchIndexes() {
        return articleQueryManager.listSearchIndexes();
    }

    @Override
    public ArticleDetailVO getArticleDetail(Long id) {
        return articleQueryManager.getArticleDetail(id);
    }

    @Override
    public AdminArticleVO getArticleForEdit(Long id) {
        return articleQueryManager.getArticleForEdit(id);
    }

    @Override
    public List<ArchiveAggregateVO> listArchives() {
        return articleQueryManager.listArchives();
    }

    @Override
    public List<ArticleCarouselVO> listCarousel() {
        return articleQueryManager.listCarousel();
    }

    @Override
    public List<ArticleSimpleVO> listArticleCardsByIds(List<Long> ids) {
        return articleQueryManager.listArticleCardsByIds(ids);
    }

    @Override
    public IPage<ArticleCardVO> pageArticles(ArticleQueryDTO queryDTO) {
        return articleQueryManager.pageArticles(queryDTO);
    }

    @Override
    public IPage<AdminArticleVO> pageAdminArticles(ArticleQueryDTO queryDTO) {
        return articleQueryManager.pageAdminArticles(queryDTO);
    }

    @Override
    public List<ArticleCategoryCountDTO> countArticleByCategoryId() {
        return articleQueryManager.countArticleByCategoryId();
    }

    @Override
    public void incrementViewCount(ArticleVisitorDTO visitorDTO) {
        articleCacheManager.incrementViewCount(visitorDTO);
    }

    /* ================= 以下为写操作，保留在 ServiceImpl 控制核心业务与事务 ================= */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(ArticleUpdateDTO updateDTO) {
        Assert.notNull(updateDTO, "更新文章参数不能为空");

        Article article = this.getById(updateDTO.getId());
        if (article == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_ARTICLE_NOT_EXIST);
        }

        if (updateDTO.getCategoryId() != null) {
            Category category = categoryService.getById(updateDTO.getCategoryId());
            if (category == null) {
                throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_CATEGORY_NOT_EXIST);
            }
        }

        // 过滤敏感词，将违规词替换为 '█'
        if (updateDTO.getContent() != null) {
            String safeContent = sensitiveWordManager.replace(updateDTO.getContent(), Constants.SENSITIVE_REPLACE_ARTICLE);
            updateDTO.setContent(safeContent);
        }

        articleConvert.updateEntityFromDto(updateDTO, article);
        this.updateById(article);
        articleTagService.updateArticleTags(updateDTO.getId(), updateDTO.getTagIds());

        // 删除该文章的详情缓存
        redisUtil.delete(RedisConstants.REDIS_ARTICLE_DETAIL_PREFIX + updateDTO.getId());
        // 删除归档和首页列表缓存
        articleCacheManager.clearListCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addArticle(ArticleAddDTO addDTO) {
        Assert.notNull(addDTO, "新增文章参数不能为空");

        Category category = categoryService.getById(addDTO.getCategoryId());
        if (category == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_CATEGORY_NOT_EXIST);
        }

        // 过滤敏感词，将违规词替换为 '█'
        if (addDTO.getContent() != null) {
            String safeContent = sensitiveWordManager.replace(addDTO.getContent(), Constants.SENSITIVE_REPLACE_ARTICLE);
            addDTO.setContent(safeContent);
        }

        Article article = articleConvert.addDtoToEntity(addDTO);

        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }
        article.setUserId(currentUser.getId());

        this.save(article);
        articleTagService.updateArticleTags(article.getId(), addDTO.getTagIds());

        // 清理列表缓存
        articleCacheManager.clearListCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticleById(Long id) {
        Assert.notNull(id, "文章ID不能为空");

        boolean success = this.lambdaUpdate()
                .eq(Article::getId, id)
                .set(Article::getIsDeleted, 1)
                .set(Article::getDeleteTime, LocalDateTime.now())
                .update();

        if (success) {
            // 删除详情
            redisUtil.delete(RedisConstants.REDIS_ARTICLE_DETAIL_PREFIX + id);
            // 删除浏览量
            redisUtil.hDel(RedisConstants.REDIS_VIEW_HASH_KEY, id.toString());
            redisUtil.setRemove(RedisConstants.REDIS_VIEW_DIRTY_SET, id);
            // 删除列表和归档
            articleCacheManager.clearListCache();

            // 触发文章删除事件
            Article article = this.getById(id);
            if (article != null) {
                Map<Long, String> deletedMap = new HashMap<>();
                deletedMap.put(article.getUserId(), article.getTitle());
                eventPublisher.publishEvent(new ArticlesDeletedEvent(this, deletedMap));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteArticles(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            log.warn("批量删除文章失败：传入的 ID 列表为空");
            return;
        }

        boolean success = this.lambdaUpdate()
                .in(Article::getId, ids)
                .set(Article::getIsDeleted, 1)
                .set(Article::getDeleteTime, LocalDateTime.now())
                .update();

        // 批量清理缓存
        if (success) {
            // 收集要删除的 Key 和 Hash Field
            List<String> detailKeys = new ArrayList<>();
            List<Object> hashFields = new ArrayList<>(); // 用于 hDel
            List<Object> dirtySetIds = new ArrayList<>(); // 用于 setRemove

            for (Long id : ids) {
                // 收集详情页 Key (String)
                detailKeys.add(RedisConstants.REDIS_ARTICLE_DETAIL_PREFIX + id);

                // 收集浏览量 ID (作为 Hash 的 Field)
                hashFields.add(id.toString());

                // 收集脏数据 ID
                dirtySetIds.add(id);
            }
            // 删除详情页缓存
            redisUtil.delete(detailKeys);

            // 删除浏览量
            if (!hashFields.isEmpty()) {
                redisUtil.hDel(RedisConstants.REDIS_VIEW_HASH_KEY, hashFields.toArray());
            }

            // 移除脏数据集合中的 ID
            if (!dirtySetIds.isEmpty()) {
                redisUtil.setRemove(RedisConstants.REDIS_VIEW_DIRTY_SET, dirtySetIds.toArray());
            }

            // 删除列表和归档
            articleCacheManager.clearListCache();

            // 批量触发文章删除事件
            List<Article> deletedArticles = this.listByIds(ids);
            if (CollUtil.isNotEmpty(deletedArticles)) {
                Map<Long, String> deletedMap = deletedArticles.stream()
                        .collect(Collectors.toMap(Article::getUserId, Article::getTitle, (t1, t2) -> t1 + "、" + t2));
                eventPublisher.publishEvent(new ArticlesDeletedEvent(this, deletedMap));
            }
        }
    }

    /**
     * 异步定时同步 Redis 中的文章阅读量到 MySQL 数据库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncArticleViewsToDb() {
        // 1. 获取所有脏数据 ID (即这期间有被访问过的文章)
        // sGet 对应 Redis: SMEMBERS key
        Set<Object> dirtyIds = redisUtil.sGet(RedisConstants.REDIS_VIEW_DIRTY_SET);

        if (CollUtil.isEmpty(dirtyIds)) {
            return; // 没有需要同步的数据
        }

        // 2. 从 Redis Hash 中批量获取这些 ID 对应的最新阅读量
        List<Object> idList = dirtyIds.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        // hMultiGet 对应 Redis: HMGET key field1 field2 ...
        // 注意：hMultiGet 返回值的顺序和传入 ID 的顺序是一致的
        List<Object> viewCounts = redisUtil.hMultiGet(RedisConstants.REDIS_VIEW_HASH_KEY, idList);

        // 3. 组装待更新的实体列表
        List<Article> updateList = new ArrayList<>();

        for (int i = 0; i < idList.size(); i++) {
            Object idObj = idList.get(i);
            Object countObj = viewCounts.get(i);

            // 判空保护 (防止 Hash 里的数据意外丢失)
            if (idObj != null && countObj != null) {
                Long articleId = Long.valueOf(((idObj.toString())));
                Long viewCount = Long.valueOf(((countObj.toString())));

                Article article = new Article();
                article.setId(articleId);
                article.setViewCount(viewCount);
                updateList.add(article);
            }
        }

        // 4. 批量更新数据库
        if (CollUtil.isNotEmpty(updateList)) {
            // 这是数据库写操作，留在 ServiceImpl 极为合理
            this.updateBatchById(updateList);

            log.info("定时任务：同步文章阅读量完成，共更新 {} 篇文章", updateList.size());

            // 5. 【重要】清除脏数据集合中的这些 ID
            redisUtil.setRemove(RedisConstants.REDIS_VIEW_DIRTY_SET, dirtyIds.toArray());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 保证级联删除的原子性
    public int clearArticleTrash(LocalDateTime recycleLimitDate) {
        if (recycleLimitDate == null) {
            return 0;
        }

        // 1. 调用 Mapper 查出所有过期的文章 ID
        List<Integer> expiredIds = baseMapper.selectExpiredArticleIds(recycleLimitDate);

        if (expiredIds == null || expiredIds.isEmpty()) {
            return 0; // 没有要清理的数据，直接返回
        }

        // 2. 级联删除标签关联
        articleTagService.remove(new LambdaQueryWrapper<ArticleTag>()
                .in(ArticleTag::getArticleId, expiredIds));

        articleFavoriteService.remove(new LambdaQueryWrapper<ArticleFavorite>().in(ArticleFavorite::getArticleId, expiredIds));
        articleLikeService.remove(new LambdaQueryWrapper<ArticleLike>().in(ArticleLike::getArticleId, expiredIds));

        // 3. 级联删除相关评论及其底下的附属数据
        commentService.clearCommentsByArticleIds(expiredIds);

        // 4. 调用 Mapper 物理删除文章本体
        return baseMapper.physicalDeleteBatchIds(expiredIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long likeArticle(Long articleId) {
        Assert.notNull(articleId, "文章ID不能为空");
        UserPayloadDTO user = UserContext.get();
        if (user == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        // 1. 调用底层服务落盘点赞记录
        articleLikeService.saveArticleLike(articleId, user.getId());

        // 2. 聚合根本身更新计数
        this.baseMapper.incrLikeCount(articleId);
        redisUtil.delete(RedisConstants.REDIS_ARTICLE_DETAIL_PREFIX + articleId);

        // 3. 发布事件，通知系统发消息
        Article article = this.getById(articleId);
        if (article != null) {
            eventPublisher.publishEvent(new InteractiveMessageEvent(
                    this,
                    article.getUserId(),
                    user.getId(),
                    BizStatus.MessageType.LIKE,
                    articleId,
                    BizStatus.MessageBizType.ARTICLE,
                    null,
                    null
            ));
            return article.getLikeCount();
        }
        return 0L;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long cancelLikeArticle(Long articleId) {
        Assert.notNull(articleId, "文章ID不能为空");
        UserPayloadDTO user = UserContext.get();
        if (user == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        // 1. 调用底层服务移除点赞记录
        articleLikeService.removeArticleLike(articleId, user.getId());

        // 2. 聚合根本身减少计数
        this.baseMapper.decrLikeCount(articleId);
        redisUtil.delete(RedisConstants.REDIS_ARTICLE_DETAIL_PREFIX + articleId);

        Article article = this.getById(articleId);
        return article != null ? article.getLikeCount() : 0L;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long favoriteArticle(Long articleId) {
        Assert.notNull(articleId, "文章ID不能为空");
        UserPayloadDTO user = UserContext.get();
        if (user == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        // 1. 调用底层服务落盘收藏记录
        articleFavoriteService.saveArticleFavorite(articleId, user.getId());

        // 2. 聚合根本身更新计数
        this.baseMapper.incrFavoriteCount(articleId);
        redisUtil.delete(RedisConstants.REDIS_ARTICLE_DETAIL_PREFIX + articleId);

        Article article = this.getById(articleId);
        return article != null ? article.getFavoriteCount() : 0L;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long cancelFavoriteArticle(Long articleId) {
        Assert.notNull(articleId, "文章ID不能为空");
        UserPayloadDTO user = UserContext.get();
        if (user == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        // 1. 调用底层服务移除收藏记录
        articleFavoriteService.removeArticleFavorite(articleId, user.getId());

        // 2. 聚合根本身减少计数
        this.baseMapper.decrFavoriteCount(articleId);
        redisUtil.delete(RedisConstants.REDIS_ARTICLE_DETAIL_PREFIX + articleId);

        Article article = this.getById(articleId);
        return article != null ? article.getFavoriteCount() : 0L;
    }
}