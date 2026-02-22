package com.example.blog.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.example.blog.exception.CustomerException;
import com.example.blog.mapper.*;
import com.example.blog.service.ArticleFavoriteService;
import com.example.blog.service.ArticleLikeService;
import com.example.blog.service.ArticleService;
import com.example.blog.service.ArticleTagService;
import com.example.blog.utils.ArticleAssembler;
import com.example.blog.utils.RedisUtil;
import com.example.blog.utils.SensitiveWordManager;
import com.example.blog.utils.UserContext;
import com.example.blog.vo.TagVO;
import com.example.blog.vo.article.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 文章业务服务实现类
 * 实现文章相关的具体业务逻辑
 */
@Slf4j
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Resource
    private ArticleConvert articleConvert;

    @Resource
    private UserMapper userMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private ArticleLikeService articleLikeService;

    @Resource
    private ArticleFavoriteService articleFavoriteService;

    @Resource
    private ArticleTagService articleTagService;

    @Resource
    private TagMapper tagMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private ArticleAssembler articleAssembler;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private SensitiveWordManager sensitiveWordManager;

    /**
     * 根据标签ID列表查询文章ID列表
     * @param tagIds 标签ID列表
     * @return 文章ID列表，如果标签为空或查询无结果返回空列表
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

    /**
     * 私有辅助方法：清理列表相关的缓存
     * (包含：归档数据、首页第一页数据)
     */
    private void clearListCache() {
        // 删除归档缓存
        redisUtil.delete(RedisConstants.REDIS_ARTICLE_ARCHIVE_KEY);
        // 删除首页第一页缓存
        redisUtil.delete(RedisConstants.REDIS_ARTICLE_LIST_FIRST_PAGE_KEY);
    }

    /**
     * 专门给 Article 实体用的浏览量同步方法
     *
     * @param articles 文章列表
     */
    private void syncViewCountForEntities(List<Article> articles) {
        if (CollUtil.isEmpty(articles)) {
            return;
        }

        // 收集所有文章的浏览量 Key
        List<Object> articleIds = articles.stream()
                .map(Article::getId)
                .map(String::valueOf)
                .collect(Collectors.toList());

        // 内存中赋值
        List<Object> viewCounts = redisUtil.hMultiGet(RedisConstants.REDIS_VIEW_HASH_KEY, articleIds);

        for (int i = 0; i < articles.size(); i++) {
            Object countObj = viewCounts.get(i);

            // 如果 Redis 里有数据，就覆盖 Entity 里的值
            // 如果 Redis 里是 null (说明没人访问过或被清空了)，就保持 DB 里的原值
            if (ObjectUtil.isNotNull(countObj)) {
                // 安全转换：先转 String 再转 Integer，防止 Redis 底层存储类型差异报错
                Long realTimeCount = Long.valueOf((countObj.toString()));
                articles.get(i).setViewCount(realTimeCount);
            }
        }
    }

    /**
     * 专门给 ArticleListVO 用的浏览量同步方法
     *
     * @param voList 文章VO列表
     */
    private void syncViewCountForVOs(List<ArticleCardVO> voList) {
        if (CollUtil.isEmpty(voList)) {
            return;
        }

        // 收集 ID
        List<Object> articleIds = voList.stream()
                .map(ArticleCardVO::getId)
                .collect(Collectors.toList());

        // 从 Redis Hash 中批量获取实时阅读量
        List<Object> viewCounts = redisUtil.hMultiGet(RedisConstants.REDIS_VIEW_HASH_KEY, articleIds);

        // 覆盖 VO 中的旧值
        for (int i = 0; i < voList.size(); i++) {
            Object countObj = viewCounts.get(i);

            // 如果 Redis Hash 里有值，就覆盖；如果是 null，说明可能被清空了，保持缓存里的原值
            if (ObjectUtil.isNotNull(countObj)) {
                try {
                    // 安全转换：Object -> String -> Integer
                    Long realTimeCount = Long.valueOf((countObj.toString()));
                    voList.get(i).setViewCount(realTimeCount);
                } catch (NumberFormatException e) {
                    // 忽略异常，保持原值
                }
            }
        }
    }

    /**
     * 获取全站搜索索引数据
     *
     * @return 文章搜索索引列表
     */
    @Override
    public List<ArticleSearchVO> listSearchIndexes() {
        List<Article> articles = lambdaQuery()
                .select(Article::getId, Article::getTitle, Article::getSummary)
                .eq(Article::getStatus, BizStatus.Article.PUBLISHED)
                .list();
        Map<String, Object> extraMaps = articleAssembler.queryBaseArticleExtraMaps(articles);
        return articleConvert.entitiesToSearchVos(articles, extraMaps);
    }

    /**
     * 前台详情查询文章
     *
     * @return 文章详情
     */
    @Override
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
            Article article = this.getById(id);
            if (article == null) {
                throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_ARTICLE_NOT_EXIST);
            }

            vo = articleConvert.entityToVo(article);

            // 填充用户信息
            User user = userMapper.selectById(article.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
                vo.setUserAvatar(user.getAvatar());
            }

            // 填充分类信息
            Category category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }

            // 填充标签信息
            List<ArticleTag> articleTags = articleTagService.list(
                    new LambdaQueryWrapper<ArticleTag>()
                            .select(ArticleTag::getTagId)
                            .eq(ArticleTag::getArticleId, id)
            );

            if (CollUtil.isNotEmpty(articleTags)) {
                List<Long> tagIds = articleTags.stream()
                        .map(ArticleTag::getTagId)
                        .collect(Collectors.toList());

                List<Tag> tags = tagMapper.selectList(
                        new LambdaQueryWrapper<Tag>()
                                .in(Tag::getId, tagIds)
                                .select(Tag::getId, Tag::getName)
                );

                if (CollUtil.isNotEmpty(tags)) {
                    vo.setTags(tags.stream()
                            .map(t -> TagVO.builder().id(t.getId()).name(t.getName()).build())
                            .collect(Collectors.toList()));
                }
            }

            // 填充评论数
            Long commentCount = commentMapper.selectCount(
                    new LambdaQueryWrapper<Comment>().eq(Comment::getArticleId, id)
            );
            vo.setCommentCount(commentCount);

            // 存入 Redis (只存对象，30分钟过期)
            redisUtil.set(articleCacheKey, vo, RedisConstants.EXPIRE_ARTICLE_DETAIL, TimeUnit.MINUTES);
        }

        // 判断当前用户是否已点赞该文章
        boolean isLiked = articleLikeService.isLikedArticle(vo.getId());
        vo.setLiked(isLiked);

        // 判断当前用户是否已收藏该文章
        boolean isFavorite = articleFavoriteService.isFavoriteArticle(vo.getId());
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

    @Override
    public AdminArticleVO getArticleForEdit(Long id) {
        Assert.notNull(id, "文章ID不能为空");

        Article article = this.getById(id);
        if (article == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_ARTICLE_NOT_EXIST);
        }
        Map<String, Object> extraMaps = articleAssembler.queryArticleExtraMaps(article);
        return articleConvert.entityToAdminVo(article, extraMaps);
    }

    /**
     * 前台归档查询文章
     *
     * @return 归档数据
     */
    @Override
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
        List<Article> articles = this.list(
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

    @Override
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

        List<Article> articles = this.list(wrapper);

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

    /**
     * 前台列表分页查询文章
     *
     * @param queryDTO 查询条件DTO
     * @return 分页结果
     */
    @Override
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
                    syncViewCountForVOs(page.getRecords());
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
        IPage<Article> articleIPage  = this.page(page, articleQueryWrapper);
        List<Article> articles = articleIPage .getRecords();
        if (CollUtil.isEmpty(articles)) {
            return articleIPage.convert(article -> null);
        }

        // 同步 Redis 中的最新浏览量到文章实体中
        syncViewCountForEntities(articles);

        Map<String, Object> extraMaps = articleAssembler.batchQueryArticleExtraMaps(articles);
        List<ArticleCardVO> vos = articleConvert.entitiesToListVos(articles, extraMaps);
        IPage<ArticleCardVO> voiPage = articleIPage.convert(article -> null);
        voiPage.setRecords(vos);
        if (isFirstPageHome) {
            redisUtil.set(RedisConstants.REDIS_ARTICLE_LIST_FIRST_PAGE_KEY, voiPage, RedisConstants.EXPIRE_ARTICLE_LIST_FIRST_PAGE, TimeUnit.MINUTES);
        }
        return voiPage;
    }

    /**
     * 后台管理分页查询文章
     *
     * @param queryDTO 查询条件DTO
     * @return 分页结果
     */
    @Override
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
                .orderByDesc(Article::getIsTop)
                .orderByDesc(Article::getCreateTime);
        IPage<Article> articleIPage  = this.page(page, articleQueryWrapper);
        List<Article> articles = articleIPage .getRecords();
        articles = CollUtil.isEmpty(articles) ? Collections.emptyList() : articles;

        // 同步 Redis 中的最新浏览量到文章实体中
        syncViewCountForEntities(articles);

        Map<String, Object> extraMaps = articleAssembler.batchQueryArticleExtraMaps(articles);
        List<AdminArticleVO> adminVos = articleConvert.entitiesToAdminVos(articles, extraMaps);
        IPage<AdminArticleVO> adminVOIPage = articleIPage.convert(article -> null);
        adminVOIPage.setRecords(adminVos);
        return adminVOIPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(ArticleUpdateDTO updateDTO) {
        Assert.notNull(updateDTO, "更新文章参数不能为空");

        Article article = this.getById(updateDTO.getId());
        if (article == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_ARTICLE_NOT_EXIST);
        }

        if (updateDTO.getCategoryId() != null) {
            Category category = categoryMapper.selectById(updateDTO.getCategoryId());
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
        clearListCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addArticle(ArticleAddDTO addDTO) {
        Assert.notNull(addDTO, "新增文章参数不能为空");

        Category category = categoryMapper.selectById(addDTO.getCategoryId());
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
        clearListCache();
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
            clearListCache();
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
            clearListCache();
        }
    }

    @Override
    public List<ArticleCategoryCountDTO> countArticleByCategoryId() {
        Integer status = BizStatus.Article.PUBLISHED.getValue();

        List<ArticleCategoryCountDTO> list = this.baseMapper.selectCountByCategoryId(status);

        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        List<Long> categoryIds = list.stream()
                .map(ArticleCategoryCountDTO::getCategoryId)
                .toList();

        List<Category> categories = categoryMapper.selectBatchIds(categoryIds);

        Map<Long, String> nameMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        for (ArticleCategoryCountDTO dto : list) {
            String name = nameMap.get(dto.getCategoryId());
            dto.setCategoryName(name);
        }

        return list;
    }

    public void incrementViewCount(ArticleVisitorDTO visitorDTO) {
        Assert.notNull(visitorDTO, "访问记录参数不能为空");

        if (visitorDTO.getArticleId() == null || StrUtil.isBlank(visitorDTO.getIp())) {
            return;
        }

        Long articleId = visitorDTO.getArticleId();
        String ip = visitorDTO.getIp();
        String userAgent = visitorDTO.getUserAgent();

        // 生成防刷指纹 (使用 Hutool 的 SecureUtil)
        String identity = ip + (StrUtil.isNotBlank(userAgent) ? userAgent : "");
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

        String hashKey = RedisConstants.REDIS_VIEW_HASH_KEY;

        try {
            // 检查 Hash 中是否有该文章的记录
            // hHasKey 对应 Redis: HEXISTS key field
            if (!redisUtil.hHasKey(hashKey, articleId.toString())) {
                // 如果 Redis 里没有，先从数据库查出来初始化
                Article article = this.getById(articleId);
                long initViewCount = (article != null && article.getViewCount() != null) ? article.getViewCount() : 0L;

                // 初始化到 Redis Hash 中
                redisUtil.hSet(hashKey, articleId.toString(), initViewCount);
            }

            // 原子自增 (Hash 结构)
            // hIncr 对应 Redis: HINCRBY key field increment
            redisUtil.hIncr(hashKey, articleId.toString(), 1L);

            // 将文章ID放入脏数据 Set，等待定时任务同步
            // sSet 对应 Redis: SADD key member
            redisUtil.sSet(RedisConstants.REDIS_VIEW_DIRTY_SET, articleId);

        } catch (Exception e) {
            log.error("Redis 计数或记录脏数据异常，文章ID: {}", articleId, e);
        }
    }

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
        List<Object> idList = new ArrayList<>(dirtyIds);
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
            // 使用 MyBatis-Plus 的 updateBatchById (需要在 ServiceImpl 中开启)
            // 如果数据量巨大 (如 > 1000 条)，建议分批处理
            this.updateBatchById(updateList);

            log.info("定时任务：同步文章阅读量完成，共更新 {} 篇文章", updateList.size());

            // 5. 【重要】清除脏数据集合中的这些 ID
            // 注意：不能直接 delete 整个 Key，因为在同步过程中可能又有新访客产生了新的脏 ID
            // 只能移除我们本次处理过的 ID
            redisUtil.setRemove(RedisConstants.REDIS_VIEW_DIRTY_SET, dirtyIds.toArray());
        }
    }

}