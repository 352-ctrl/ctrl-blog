package com.example.blog.modules.article.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.common.utils.RedisUtil;
import com.example.blog.core.exception.CustomerException;
import com.example.blog.core.security.UserContext;
import com.example.blog.modules.article.builder.ArticleContextBuilder;
import com.example.blog.modules.article.mapper.ArticleMapper;
import com.example.blog.modules.article.model.bo.ArticleExtraContext;
import com.example.blog.modules.article.model.convert.ArticleConvert;
import com.example.blog.modules.article.model.dto.ArticleCategoryCountDTO;
import com.example.blog.modules.article.model.dto.ArticleQueryDTO;
import com.example.blog.modules.article.model.entity.Article;
import com.example.blog.modules.article.model.entity.Category;
import com.example.blog.modules.article.model.vo.*;
import com.example.blog.modules.article.service.*;
import com.example.blog.modules.operation.model.entity.Comment;
import com.example.blog.modules.operation.service.CommentService;
import com.example.blog.modules.user.model.dto.UserPayloadDTO;
import com.example.blog.modules.user.model.entity.User;
import com.example.blog.modules.user.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 文章查询管理器 (CQRS - Query侧)
 * 专门负责前后台所有复杂的文章聚合查询、分页、列表组装逻辑
 */
@Slf4j
@Component
public class ArticleQueryManager {

    @Resource
    private ArticleMapper articleMapper;
    @Resource
    private ArticleTagService articleTagService;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private ArticleConvert articleConvert;
    @Resource
    private UserService userService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private TagService tagService;
    @Resource
    private CommentService commentService;
    @Resource
    private ArticleLikeService articleLikeService;
    @Resource
    private ArticleFavoriteService articleFavoriteService;
    @Resource
    private ArticleContextBuilder articleContextBuilder;
    @Resource
    private ArticleCacheManager articleCacheManager;

    /**
     * 根据标签ID列表查询文章ID列表
     */
    private List<Long> getArticleIdsByTagIds(List<Long> tagIds) {
        if (CollUtil.isEmpty(tagIds)) {
            return Collections.emptyList();
        }
        // 确保标签ID去重
        List<Long> distinctTagIds = tagIds.stream().distinct().toList();

        List<Long> articleIds = articleTagService.listArticleIdsByTagIds(distinctTagIds);

        if (CollUtil.isEmpty(articleIds)) {
            return Collections.emptyList();
        }

        return articleIds;
    }

    @SuppressWarnings("unchecked")
    public List<ArticleSimpleVO> listHotArticles() {
        // 1. 定义 Redis Key
        String cacheKey = RedisConstants.REDIS_ARTICLE_HOT_KEY;

        // 2. 尝试从 Redis 获取缓存
        Object cacheValue = redisUtil.get(cacheKey);
        if (cacheValue != null) {
            return (List<ArticleSimpleVO>) cacheValue;
        }

        // 3. 缓存未命中，查询数据库
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Article::getId, Article::getTitle, Article::getViewCount, Article::getCover, Article::getCreateTime)
                .eq(Article::getStatus, BizStatus.Article.PUBLISHED)
                // 使用 last 拼接复杂的加权排序 SQL
                // 权重公式：阅读量(x1) + 点赞量(x5) + 收藏量(x10)
                .last("ORDER BY (view_count + like_count * 5 + favorite_count * 10) DESC LIMIT 5");

        List<Article> articles = articleMapper.selectList(wrapper);

        if (CollUtil.isEmpty(articles)) {
            return Collections.emptyList();
        }

        // 4. 同步 Redis 中的最新浏览量到实体中
        articleCacheManager.syncViewCount(articles, Article::getId, Article::setViewCount);

        // 5. 实体类转 VO
        List<ArticleSimpleVO> voList = articleConvert.entitiesToSimpleVos(articles);

        // 6. 写入 Redis
        redisUtil.set(cacheKey, voList, RedisConstants.EXPIRE_ARTICLE_HOT, TimeUnit.HOURS);

        return voList;
    }

    public List<ArticleSearchVO> listSearchIndexes() {
        List<Article> articles = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                .select(Article::getId, Article::getTitle, Article::getSummary)
                .eq(Article::getStatus, BizStatus.Article.PUBLISHED));
        ArticleExtraContext context = articleContextBuilder.queryBaseArticleExtraContext(articles);
        return articleConvert.entitiesToSearchVos(articles, context);
    }

    public ArticleDetailVO getArticleDetail(Long id) {
        Assert.notNull(id, "文章ID不能为空");

        String articleCacheKey = RedisConstants.REDIS_ARTICLE_DETAIL_PREFIX + id;
        // 尝试从 Redis 获取完整的 VO 对象
        ArticleDetailVO vo = null;
        try {
            // 尝试获取，如果反序列化失败（脏数据），捕获异常不报错
            vo = (ArticleDetailVO) redisUtil.get(articleCacheKey);
        } catch (Exception e) {
            log.error("Redis文章详情数据异常，Key: {}", articleCacheKey);
            redisUtil.delete(articleCacheKey); // 删除坏数据
        }
        // 如果缓存不存在，查询数据库并组装
        if (vo == null) {
            Article article = articleMapper.selectById(id);
            if (article == null) {
                throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_ARTICLE_NOT_EXIST);
            }

            vo = articleConvert.entityToVo(article);

            // 填充用户信息
            User user = userService.getById(article.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
                vo.setUserAvatar(user.getAvatar());
            } else {
                vo.setUserNickname(Constants.DEFAULT_UNKNOWN_NICKNAME);
                vo.setUserAvatar(StrUtil.EMPTY);
            }

            // 填充分类信息
            Category category = categoryService.getById(article.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }

            // 填充标签信息
            List<TagVO> tags = tagService.listTagsByArticleId(id);
            if (CollUtil.isNotEmpty(tags)) {
                vo.setTags(tags);
            }

            // 填充评论数
            long commentCount = commentService.count(new LambdaQueryWrapper<Comment>().eq(Comment::getArticleId, id));
            vo.setCommentCount(commentCount);

            // 存入 Redis (只存对象，30分钟过期)
            redisUtil.set(articleCacheKey, vo, RedisConstants.EXPIRE_ARTICLE_DETAIL, TimeUnit.MINUTES);
        }

        UserPayloadDTO currentUser = UserContext.get();
        Long userId = currentUser != null ? currentUser.getId() : null;

        // 判断当前用户是否已点赞该文章
        boolean isLiked = articleLikeService.isLikedArticle(vo.getId(), userId);
        vo.setLiked(isLiked);

        // 判断当前用户是否已收藏该文章
        boolean isFavorite = articleFavoriteService.isFavoriteArticle(vo.getId(), userId);
        vo.setFavorite(isFavorite);

        // 使用 Hash 结构的 Key
        String hashKey = RedisConstants.REDIS_VIEW_HASH_KEY;
        // 从 Hash 中获取该文章的最新阅读量
        Object viewCountObj = redisUtil.hGet(hashKey, id.toString());
        if (viewCountObj != null) {
            // 如果 Redis Hash 里有数据，直接覆盖 VO 里的值
            try {
                vo.setViewCount(Long.valueOf((viewCountObj.toString())));
            } catch (NumberFormatException e) {
                log.warn("Redis阅读量格式异常: {}", viewCountObj);
            }
        } else {
            // 场景：Redis Hash 里丢数据了（可能是过期或误删），但我们手里有 vo 对象
            // 动作：利用手中的 vo 数据，反向修复 Redis Hash，实现“缓存预热/自愈”
            // 注意：vo.getViewCount() 可能是 null，要判空，或者默认 0
            Long initViewCount = vo.getViewCount() != null ? vo.getViewCount() : 0;

            // 重新塞回 Hash 中，这样下一个访客进来 increment 时就不用查库了
            try {
                redisUtil.hSet(hashKey, id.toString(), initViewCount);
            } catch (Exception e) {
                // 吞掉异常，不要因为这个次要逻辑影响主业务返回详情
                log.warn("Redis阅读量反向初始化失败: {}", e.getMessage());
            }
        }

        return vo;
    }

    public AdminArticleVO getArticleForEdit(Long id) {
        Assert.notNull(id, "文章ID不能为空");

        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_ARTICLE_NOT_EXIST);
        }
        ArticleExtraContext context = articleContextBuilder.queryArticleExtraContext(article); // 修改此处
        return articleConvert.entityToAdminVo(article, context);
    }

    @SuppressWarnings("unchecked")
    public List<ArchiveAggregateVO> listArchives() {
        // 尝试从 Redis 获取
        try {
            List<ArchiveAggregateVO> cachedArchive = (List<ArchiveAggregateVO>) redisUtil.get(RedisConstants.REDIS_ARTICLE_ARCHIVE_KEY);
            if (cachedArchive != null) {
                return cachedArchive;
            }
        } catch (Exception e) {
            log.error("Redis归档数据异常，Key: " + RedisConstants.REDIS_ARTICLE_ARCHIVE_KEY);
            redisUtil.delete(RedisConstants.REDIS_ARTICLE_ARCHIVE_KEY);
        }

        // 查询数据库
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .select(Article::getId, Article::getTitle, Article::getCreateTime)
                        .eq(Article::getStatus, BizStatus.Article.PUBLISHED)
                        .orderByDesc(Article::getCreateTime)
        );

        // 转换为ArchiveVO
        List<ArticleArchiveVO> articleArchiveVOS = articleConvert.entitiesToArchiveVos(articles);

        // 按年份分组
        Map<Integer, List<ArticleArchiveVO>> yearMap = new LinkedHashMap<>();

        for (ArticleArchiveVO articleArchiveVO : articleArchiveVOS) {
            // 从创建时间中提取年份
            String createTimeStr = String.valueOf(articleArchiveVO.getCreateTime()); // 格式：yyyy-MM-dd HH:mm:ss
            int year = Integer.parseInt(createTimeStr.substring(0, 4));

            // 按年份分组
            yearMap.computeIfAbsent(year, k -> new ArrayList<>()).add(articleArchiveVO);
        }

        // 按年份倒序排列（最新的年份在前）
        List<ArchiveAggregateVO> result = yearMap.entrySet().stream()
                // 按年份倒序
                .sorted(Map.Entry.<Integer, List<ArticleArchiveVO>>comparingByKey().reversed())
                // 映射为 VO 对象
                .map(entry -> ArchiveAggregateVO.builder()
                        .year(entry.getKey())
                        .count(entry.getValue().size())
                        .articles(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        // 写入 Redis (设置1小时过期)
        redisUtil.set(RedisConstants.REDIS_ARTICLE_ARCHIVE_KEY, result, RedisConstants.EXPIRE_ARTICLE_ARCHIVE, TimeUnit.HOURS);

        return result;
    }

    public List<ArticleCarouselVO> listCarousel() {
        // 1. 获取 Redis Key
        String cacheKey = RedisConstants.REDIS_ARTICLE_CAROUSEL_KEY;

        // 2. 查询缓存
        Object cacheValue = redisUtil.get(cacheKey);
        if (cacheValue != null) {
            return (List<ArticleCarouselVO>) cacheValue;
        }

        // 3. 缓存未命中，查询数据库
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Article::getId, Article::getTitle, Article::getCover, Article::getCreateTime) // 只查需要的字段，优化性能
                .eq(Article::getIsCarousel, BizStatus.Common.ENABLE)        // 必须是轮播图
                .eq(Article::getStatus, BizStatus.Article.PUBLISHED)            // 必须是已发布
                .orderByDesc(Article::getCreateTime)  // 按时间倒序
                .last("limit 5");                     // 限制 5 条

        List<Article> articles = articleMapper.selectList(wrapper);

        // 4. 判空处理 (防止缓存穿透，也可以选择缓存空列表)
        if (articles == null || articles.isEmpty()) {
            return Collections.emptyList();
        }

        // 5. 实体类转 VO
        List<ArticleCarouselVO> voList = articleConvert.entitiesToCarouseVos(articles);

        // 6. 写入 Redis
        redisUtil.set(cacheKey, voList, RedisConstants.EXPIRE_ARTICLE_CAROUSEL, TimeUnit.HOURS);

        return voList;
    }

    public List<ArticleSimpleVO> listArticleCardsByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }

        // 1. 批量查询数据库
        List<Article> articles = articleMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(articles)) {
            return Collections.emptyList();
        }

        // 2. 按传入的 ids 顺序进行内存重排序！
        Map<Long, Article> articleMap = articles.stream()
                .collect(Collectors.toMap(Article::getId, a -> a));

        List<Article> sortedArticles = ids.stream()
                .map(articleMap::get)
                .filter(Objects::nonNull) // 过滤掉可能已经被物理删除的文章
                .toList();

        // 3. 转换并返回
        return articleConvert.entitiesToSimpleVos(sortedArticles);
    }

    @SuppressWarnings("unchecked")
    public IPage<ArticleCardVO> pageArticles(ArticleQueryDTO queryDTO) {
        Assert.notNull(queryDTO, "查询条件不能为空");

        // 判断是否是“首页第一页”（无搜索、无分类、无标签、第1页）
        boolean isFirstPageHome = queryDTO.getPageNum() == 1
                && StrUtil.isBlank(queryDTO.getTitle())
                && queryDTO.getCategoryId() == null
                && CollUtil.isEmpty(queryDTO.getTagIds());

        if (isFirstPageHome) {
            try {
                Object cache = redisUtil.get(RedisConstants.REDIS_ARTICLE_LIST_FIRST_PAGE_KEY);
                if (cache != null) {
                    IPage<ArticleCardVO> page = (IPage<ArticleCardVO>) cache;
                    articleCacheManager.syncViewCount(page.getRecords(), ArticleCardVO::getId, ArticleCardVO::setViewCount);
                    return page;
                }
            } catch (Exception e) {
                log.error("Redis首页列表数据异常，Key: " + RedisConstants.REDIS_ARTICLE_LIST_FIRST_PAGE_KEY);
                redisUtil.delete(RedisConstants.REDIS_ARTICLE_LIST_FIRST_PAGE_KEY);
            }
        }

        Page<Article> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Article> articleQueryWrapper = new LambdaQueryWrapper<>();
        if (CollUtil.isNotEmpty(queryDTO.getTagIds())) {
            List<Long> articleIds = getArticleIdsByTagIds(queryDTO.getTagIds());

            if (CollUtil.isEmpty(articleIds)) {
                return new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
            }

            articleQueryWrapper.in(Article::getId, articleIds);
        }
        articleQueryWrapper.like(StrUtil.isNotBlank(queryDTO.getTitle()), Article::getTitle, queryDTO.getTitle())
                .eq(queryDTO.getCategoryId() != null, Article::getCategoryId, queryDTO.getCategoryId())
                .eq(Article::getStatus, BizStatus.Article.PUBLISHED)
                .orderByDesc(Article::getIsTop)
                .orderByDesc(Article::getCreateTime);
        IPage<Article> articleIPage  = articleMapper.selectPage(page, articleQueryWrapper);
        List<Article> articles = articleIPage .getRecords();
        if (CollUtil.isEmpty(articles)) {
            return articleIPage.convert(article -> null);
        }

        // 同步 Redis 中的最新浏览量到文章实体中
        articleCacheManager.syncViewCount(articles, Article::getId, Article::setViewCount);

        ArticleExtraContext context = articleContextBuilder.batchQueryArticleExtraContext(articles); // 修改此处
        List<ArticleCardVO> vos = articleConvert.entitiesToListVos(articles, context);
        IPage<ArticleCardVO> voiPage = articleIPage.convert(article -> null);
        voiPage.setRecords(vos);
        if (isFirstPageHome) {
            redisUtil.set(RedisConstants.REDIS_ARTICLE_LIST_FIRST_PAGE_KEY, voiPage, RedisConstants.EXPIRE_ARTICLE_LIST_FIRST_PAGE, TimeUnit.MINUTES);
        }
        return voiPage;
    }

    public IPage<AdminArticleVO> pageAdminArticles(ArticleQueryDTO queryDTO) {
        Assert.notNull(queryDTO, "查询条件不能为空");

        Page<Article> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Article> articleQueryWrapper = new LambdaQueryWrapper<>();
        if (CollUtil.isNotEmpty(queryDTO.getTagIds())) {
            List<Long> articleIds = getArticleIdsByTagIds(queryDTO.getTagIds());

            if (CollUtil.isEmpty(articleIds)) {
                return new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
            }

            articleQueryWrapper.in(Article::getId, articleIds);
        }
        articleQueryWrapper.like(StrUtil.isNotBlank(queryDTO.getTitle()), Article::getTitle, queryDTO.getTitle())
                .eq(queryDTO.getCategoryId() != null, Article::getCategoryId, queryDTO.getCategoryId())
                .eq(queryDTO.getStatus() != null, Article::getStatus, queryDTO.getStatus()) // 新增：文章状态精准查询
                .eq(queryDTO.getIsTop() != null, Article::getIsTop, queryDTO.getIsTop())    // 新增：置顶状态精准查询
                .orderByDesc(Article::getIsTop)
                .orderByDesc(Article::getCreateTime);
        IPage<Article> articleIPage  = articleMapper.selectPage(page, articleQueryWrapper);
        List<Article> articles = articleIPage .getRecords();
        articles = CollUtil.isEmpty(articles) ? Collections.emptyList() : articles;

        // 同步 Redis 中的最新浏览量到文章实体中
        articleCacheManager.syncViewCount(articles, Article::getId, Article::setViewCount);

        ArticleExtraContext context = articleContextBuilder.batchQueryArticleExtraContext(articles); // 修改此处
        List<AdminArticleVO> adminVos = articleConvert.entitiesToAdminVos(articles, context);
        IPage<AdminArticleVO> adminVOIPage = articleIPage.convert(article -> null);
        adminVOIPage.setRecords(adminVos);
        return adminVOIPage;
    }

    public List<ArticleCategoryCountDTO> countArticleByCategoryId() {
        Integer status = BizStatus.Article.PUBLISHED.getValue();

        List<ArticleCategoryCountDTO> list = articleMapper.selectCountByCategoryId(status);

        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        List<Long> categoryIds = list.stream()
                .map(ArticleCategoryCountDTO::getCategoryId)
                .toList();

        List<Category> categories = categoryService.listByIds(categoryIds);

        Map<Long, String> nameMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        for (ArticleCategoryCountDTO dto : list) {
            String name = nameMap.get(dto.getCategoryId());
            dto.setCategoryName(name);
        }

        return list;
    }
}