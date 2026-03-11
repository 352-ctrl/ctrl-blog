package com.example.blog.modules.user.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.modules.operation.service.CommentService;
import com.example.blog.modules.user.model.convert.UserConvert;
import com.example.blog.modules.operation.model.dto.EmailRequestDTO;
import com.example.blog.common.base.PageQueryDTO;
import com.example.blog.modules.user.model.dto.UserChangeEmailDTO;
import com.example.blog.modules.user.model.dto.UserChangePwdDTO;
import com.example.blog.modules.user.model.dto.UserPayloadDTO;
import com.example.blog.modules.user.model.dto.UserProfileUpdateDTO;
import com.example.blog.modules.article.model.entity.Article;
import com.example.blog.modules.article.model.entity.ArticleFavorite;
import com.example.blog.modules.article.model.entity.ArticleLike;
import com.example.blog.modules.operation.model.entity.Comment;
import com.example.blog.modules.user.model.entity.User;
import com.example.blog.core.exception.CustomerException;
import com.example.blog.modules.user.mapper.UserMapper;
import com.example.blog.modules.article.service.ArticleFavoriteService;
import com.example.blog.modules.article.service.ArticleLikeService;
import com.example.blog.modules.article.service.ArticleService;
import com.example.blog.modules.user.service.AuthService;
import com.example.blog.modules.user.service.UserProfileService;
import com.example.blog.common.utils.MailService;
import com.example.blog.core.security.PasswordEncoderUtil;
import com.example.blog.common.utils.RedisUtil;
import com.example.blog.core.security.UserContext;
import com.example.blog.modules.user.model.vo.UserDashboardVO;
import com.example.blog.modules.article.model.vo.ArticleSimpleVO;
import com.example.blog.modules.operation.model.vo.UserCommentVO;
import com.example.blog.modules.user.model.vo.UserVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 个人信息业务服务实现类
 * 实现个人信息相关的具体业务逻辑
 */
@Service
public class UserProfileServiceImpl extends ServiceImpl<UserMapper, User> implements UserProfileService {

    @Resource
    private UserConvert userConvert;

    @Resource
    private AuthService authService;

    @Resource
    private CommentService commentService;

    @Resource
    private ArticleLikeService articleLikeService;

    @Resource
    private ArticleFavoriteService articleFavoriteService;

    @Resource
    private ArticleService articleService;

    @Resource
    MailService mailService;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 通用用户修改操作前置校验
     * @param id 要更新的用户ID
     * @return 校验通过后的用户对象
     * @throws CustomerException 校验失败时抛出异常
     */
    private User validateUserForUpdate(Long id) {
        Assert.notNull(id, "用户ID不能为空");

        // 登录状态校验
        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null || currentUser.getId() == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        // 权限校验
        if (!id.equals(currentUser.getId())) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NO_PERMISSION);
        }

        // 验证用户是否存在
        User user = this.getById(id);
        if (user == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_USER_NOT_EXIST);
        }

        return user;
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    @Override
    public UserVO getProfile() {
        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null || currentUser.getId() == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        Long userId = currentUser.getId();
        String cacheKey = RedisConstants.REDIS_USER_INFO_KEY + userId;

        // 1. 尝试从缓存获取
        UserVO cachedUser = (UserVO) redisUtil.get(cacheKey);
        if (cachedUser != null) {
            return cachedUser;
        }

        // 2. 缓存不存在，查询数据库
        User user = this.getById(currentUser.getId());
        if (user == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_USER_NOT_EXIST);
        }

        UserVO userVO = userConvert.entityToVo(user);

        redisUtil.set(cacheKey, userVO, RedisConstants.EXPIRE_USER_INFO, TimeUnit.MINUTES);

        return userVO;
    }

    @Override
    public UserDashboardVO getUserDashboardData() {
        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }
        Long userId = currentUser.getId();

        // 1. 统计数据
        // 1.1 累计评论数
        long commentCount = commentService.count(
                new LambdaQueryWrapper<Comment>().eq(Comment::getUserId, userId)
        );

        // 1.2 累计点赞数 (文章点赞)
        long likeCount = articleLikeService.count(
                new LambdaQueryWrapper<ArticleLike>().eq(ArticleLike::getUserId, userId)
        );

        // 1.3 累计收藏数
        long favoriteCount = articleFavoriteService.count(
                new LambdaQueryWrapper<ArticleFavorite>().eq(ArticleFavorite::getUserId, userId)
        );

        UserDashboardVO.Count countData = UserDashboardVO.Count.builder()
                .comment(commentCount)
                .like(likeCount)
                .favorite(favoriteCount)
                .build();

        // 2. 近期动态 (联表或分步查询)
        // 2.1 获取最近收藏的 3 篇文章 ID
        List<Long> recentFavoriteArticleIds = articleFavoriteService.list(
                new LambdaQueryWrapper<ArticleFavorite>()
                        .select(ArticleFavorite::getArticleId)
                        .eq(ArticleFavorite::getUserId, userId)
                        .orderByDesc(ArticleFavorite::getCreateTime)
                        .last("LIMIT 3")
        ).stream().map(ArticleFavorite::getArticleId).toList();

        // 2.2 获取最近点赞的 3 篇文章 ID
        List<Long> recentLikeArticleIds = articleLikeService.list(
                new LambdaQueryWrapper<ArticleLike>()
                        .select(ArticleLike::getArticleId)
                        .eq(ArticleLike::getUserId, userId)
                        .orderByDesc(ArticleLike::getCreateTime)
                        .last("LIMIT 3")
        ).stream().map(ArticleLike::getArticleId).toList();

        // 2.3 组装文章详细信息 (如果有数据的话)
        List<ArticleSimpleVO> recentFavorites = articleService.listArticleCardsByIds(recentFavoriteArticleIds);
        List<ArticleSimpleVO> recentLikes = articleService.listArticleCardsByIds(recentLikeArticleIds);

        // 3. 构建返回结果
        return UserDashboardVO.builder()
                .count(countData)
                .recentFavorites(recentFavorites)
                .recentLikes(recentLikes)
                .build();
    }

    @Override
    public IPage<ArticleSimpleVO> pageMyFavorites(PageQueryDTO queryDTO) {
        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        // 1. 分页查询收藏记录
        Page<ArticleFavorite> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<ArticleFavorite> favoritePage = articleFavoriteService.page(page,
                new LambdaQueryWrapper<ArticleFavorite>()
                        .eq(ArticleFavorite::getUserId, currentUser.getId())
                        .orderByDesc(ArticleFavorite::getCreateTime)
        );

        // 2. 构建返回的 VO 分页对象
        Page<ArticleSimpleVO> voPage = new Page<>(favoritePage.getCurrent(), favoritePage.getSize(), favoritePage.getTotal());

        // 3. 提取文章 ID 列表
        List<Long> articleIds = favoritePage.getRecords().stream().map(ArticleFavorite::getArticleId).toList();
        if (CollUtil.isNotEmpty(articleIds)) {
            // 批量查询文章信息
            List<ArticleSimpleVO> articles = articleService.listArticleCardsByIds(articleIds);
            // 转换为 Map 以便根据原收藏时间顺序进行映射装配
            Map<Long, ArticleSimpleVO> articleMap = articles.stream().collect(Collectors.toMap(ArticleSimpleVO::getId, a -> a));

            List<ArticleSimpleVO> sortedArticles = articleIds.stream()
                    .map(articleMap::get)
                    .filter(Objects::nonNull)
                    .toList();
            voPage.setRecords(sortedArticles);
        }
        return voPage;
    }

    @Override
    public IPage<ArticleSimpleVO> pageMyLikes(PageQueryDTO queryDTO) {
        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        // 1. 分页查询点赞记录
        Page<ArticleLike> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<ArticleLike> likePage = articleLikeService.page(page,
                new LambdaQueryWrapper<ArticleLike>()
                        .eq(ArticleLike::getUserId, currentUser.getId())
                        .orderByDesc(ArticleLike::getCreateTime)
        );

        // 2. 构建返回的 VO 分页对象
        Page<ArticleSimpleVO> voPage = new Page<>(likePage.getCurrent(), likePage.getSize(), likePage.getTotal());

        // 3. 提取文章 ID 列表
        List<Long> articleIds = likePage.getRecords().stream().map(ArticleLike::getArticleId).toList();
        if (CollUtil.isNotEmpty(articleIds)) {
            List<ArticleSimpleVO> articles = articleService.listArticleCardsByIds(articleIds);
            Map<Long, ArticleSimpleVO> articleMap = articles.stream().collect(Collectors.toMap(ArticleSimpleVO::getId, a -> a));

            List<ArticleSimpleVO> sortedArticles = articleIds.stream()
                    .map(articleMap::get)
                    .filter(Objects::nonNull)
                    .toList();
            voPage.setRecords(sortedArticles);
        }
        return voPage;
    }

    @Override
    public IPage<UserCommentVO> pageMyComments(PageQueryDTO queryDTO) {
        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        // 1. 分页查询评论表
        Page<Comment> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<Comment> commentPage = commentService.page(page,
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getUserId, currentUser.getId())
                        .orderByDesc(Comment::getCreateTime)
        );

        // 2. 提取文章 IDs 以便批量查询文章标题，防止 N+1 查询问题
        List<Long> articleIds = commentPage.getRecords().stream()
                .map(Comment::getArticleId)
                .distinct()
                .toList();

        Map<Long, String> articleTitleMap = new HashMap<>();
        if (CollUtil.isNotEmpty(articleIds)) {
            List<Article> articles = articleService.list(
                    new LambdaQueryWrapper<Article>()
                            .select(Article::getId, Article::getTitle)
                            .in(Article::getId, articleIds)
            );
            articles.forEach(a -> articleTitleMap.put(a.getId(), a.getTitle()));
        }

        // 3. 将 Entity 转换为 UserCommentVO
        return commentPage.convert(comment -> UserCommentVO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createTime(comment.getCreateTime())
                .articleId(comment.getArticleId())
                .articleTitle(articleTitleMap.getOrDefault(comment.getArticleId(), "文章已删除"))
                .build());
    }

    /**
     * 更新用户基本信息（个人信息设置）
     *
     * @param updateDTO 用户信息更新DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(UserProfileUpdateDTO updateDTO) {
        Assert.notNull(updateDTO, "更新用户信息参数不能为空");

        UserPayloadDTO currentUser = UserContext.get();
        // 基础校验
        if (currentUser == null || currentUser.getId() == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }
        // 检查是否有内容需要更新
        boolean hasUpdate = false;
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, currentUser.getId());

        if (StrUtil.isNotBlank(updateDTO.getNickname())) {
            updateWrapper.set(User::getNickname, updateDTO.getNickname());
            hasUpdate = true;
        }
        if (StrUtil.isNotBlank(updateDTO.getAvatar())) {
            updateWrapper.set(User::getAvatar, updateDTO.getAvatar());
            hasUpdate = true;
        }
        // 这里使用 != null 判断而不是 isNotBlank。因为如果用户传了 "" (空字符串)，说明用户想要清空个人简介，这也是一种合法的更新行为。
        if (updateDTO.getBio() != null) {
            updateWrapper.set(User::getBio, updateDTO.getBio());
            hasUpdate = true;
        }

        // 如果没有字段需要更新，直接返回，不查库不删缓存
        if (!hasUpdate) {
            return;
        }

        boolean success = this.update(null, updateWrapper);
        if (!success) {
            throw new CustomerException(MessageConstants.MSG_UPDATE_FAILED);
        }

        // 删除 Redis 缓存
        redisUtil.delete(RedisConstants.REDIS_USER_INFO_KEY + currentUser.getId());

        if (BizStatus.Role.SUPER_ADMIN.equals(currentUser.getRole())) {
            redisUtil.delete(RedisConstants.REDIS_WEBMASTER_INFO_KEY);
        }

        // 只有管理员和超级管理员修改资料，才有可能影响文章展示，才去清理文章缓存
        boolean isAdminOrSuperAdmin = BizStatus.Role.ADMIN.equals(currentUser.getRole()) ||
                BizStatus.Role.SUPER_ADMIN.equals(currentUser.getRole());

        // 如果修改了昵称或头像，清除关联的文章缓存
        if (isAdminOrSuperAdmin && (StrUtil.isNotBlank(updateDTO.getNickname()) || StrUtil.isNotBlank(updateDTO.getAvatar()))) {

            // 2.1 清除公共文章列表缓存 (首页、热门、轮播)
            redisUtil.delete(RedisConstants.REDIS_ARTICLE_LIST_FIRST_PAGE_KEY);
            redisUtil.delete(RedisConstants.REDIS_ARTICLE_HOT_KEY);
            redisUtil.delete(RedisConstants.REDIS_ARTICLE_CAROUSEL_KEY);

            // 2.2 查找该用户发布的所有文章，精准清除文章详情缓存
            // 注意：如果你顶部没有引入 Article 实体，请引入 import com.example.blog.modules.article.model.entity.Article;
            List<Article> userArticles = articleService.list(
                    new LambdaQueryWrapper<Article>()
                            .select(Article::getId)
                            .eq(Article::getUserId, currentUser.getId())
            );

            if (CollUtil.isNotEmpty(userArticles)) {
                List<String> articleCacheKeys = userArticles.stream()
                        .map(article -> RedisConstants.REDIS_ARTICLE_DETAIL_PREFIX + article.getId())
                        .toList(); // 如果你的 JDK 版本低于 16，请换成 .collect(Collectors.toList())
                redisUtil.delete(articleCacheKeys);
            }
        }
    }

    @Override
    public void sendBindEmailCode(EmailRequestDTO emailRequestDTO) {
        Assert.hasText(emailRequestDTO.getEmail(), "新邮箱不能为空");

        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        // 1. 校验新邮箱是否被其他用户占用 (复用现有常量)
        long count = this.count(new LambdaQueryWrapper<User>().eq(User::getEmail, emailRequestDTO.getEmail()));
        if (count > 0) {
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_EMAIL_EXIST);
        }

        // 2. 防刷校验：动态计算过期时间阈值
        // 只要剩余时间 > 240秒，说明距离上次发邮件还不到 1 分钟
        String redisKey = RedisConstants.REDIS_EMAIL_BIND_CODE_KEY + emailRequestDTO.getEmail();
        long expire = java.util.Optional.ofNullable(redisUtil.getExpire(redisKey, TimeUnit.SECONDS)).orElse(-2L);
        long frequencyThreshold = (RedisConstants.EXPIRE_EMAIL_CODE * 60) - 60;

        if (expire > frequencyThreshold) {
            throw new CustomerException(ResultCode.TOO_MANY_REQUESTS, MessageConstants.MSG_SEND_FREQUENTLY);
        }

        // 3. 生成 6 位数字验证码
        String code = cn.hutool.core.util.RandomUtil.randomNumbers(6);

        // 4. 存入 Redis
        redisUtil.setStr(redisKey, code, RedisConstants.EXPIRE_EMAIL_CODE, TimeUnit.MINUTES);

        // 5. 异步发送邮件
        Map<String, Object> model = new java.util.HashMap<>();
        model.put("code", code);
        model.put("title", Constants.EMAIL_TITLE_BIND);
        mailService.sendHtmlMail(emailRequestDTO.getEmail(), Constants.EMAIL_SUBJECT_BIND, Constants.TEMPLATE_REGISTER_CODE, model);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeEmail(UserChangeEmailDTO changeEmailDTO, String token) {
        Assert.notNull(changeEmailDTO, "换绑参数不能为空");

        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        String newEmail = changeEmailDTO.getNewEmail();

        // 1. 再次校验邮箱是否已被占用 (防止并发情况下的复用)
        long count = this.count(new LambdaQueryWrapper<User>().eq(User::getEmail, newEmail));
        if (count > 0) {
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_EMAIL_EXIST);
        }

        // 2. 校验验证码
        String redisKey = RedisConstants.REDIS_EMAIL_BIND_CODE_KEY + newEmail;
        String cacheCode = redisUtil.getStr(redisKey);

        if (cacheCode == null) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_CODE_EXPIRED);
        }
        if (!cacheCode.equals(changeEmailDTO.getCode())) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_CODE_ERROR);
        }

        // 3. 执行更新
        boolean success = this.lambdaUpdate()
                .eq(User::getId, currentUser.getId())
                .set(User::getEmail, newEmail)
                .update();

        if (!success) {
            throw new CustomerException(MessageConstants.MSG_UPDATE_FAILED);
        }

        // 4. 更新成功后，删除验证码缓存
        redisUtil.delete(redisKey);

        // 5. 删除用户信息缓存，调用 AuthService 拉黑 Token 强制重新登录
        redisUtil.delete(RedisConstants.REDIS_USER_INFO_KEY + currentUser.getId());
        if (StrUtil.isNotBlank(token)) {
            authService.logout(token);
        }
    }

    /**
     * 修改密码
     *
     * @param changePwdDTO 用户修改密码DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(UserChangePwdDTO changePwdDTO, String token) {
        Assert.notNull(changePwdDTO, "修改密码参数不能为空");

        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser.getId() == null) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_PARAM_ERROR);
        }

        User user = validateUserForUpdate(currentUser.getId());

        // 验证原密码
        if (!PasswordEncoderUtil.matches(changePwdDTO.getOldPassword(), user.getPassword())) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_OLD_PASSWORD_ERROR);
        }

        // 验证原密码与新密码是否相同
        if (PasswordEncoderUtil.matches(changePwdDTO.getNewPassword(), user.getPassword())) {
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_NEW_PASSWORD_SAME_AS_OLD);
        }

        // 密码加密（核心：明文→BCrypt哈希）
        String encryptedPassword = PasswordEncoderUtil.encode(changePwdDTO.getNewPassword());

        // 更新密码
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getPassword, encryptedPassword)
                .eq(User::getId, currentUser.getId());

        boolean success = this.update(null, updateWrapper);
        if (!success) {
            throw new CustomerException(MessageConstants.MSG_UPDATE_FAILED);
        }

        // 删除 Redis 缓存
        redisUtil.delete(RedisConstants.REDIS_USER_INFO_KEY + currentUser.getId());

        // 调用 AuthService 的 logout 方法拉黑 Token
        if (StrUtil.isNotBlank(token)) {
            authService.logout(token);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void requestAccountDeletion(String token) {
        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        // 获取并校验用户状态
        User user = this.getById(currentUser.getId());
        if (user == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_USER_NOT_EXIST);
        }

        user.setStatus(BizStatus.User.PENDING_DELETION);
        user.setCancelTime(LocalDateTime.now());       // 记录当下时间作为冷静期起点

        // 更新数据库
        boolean success = this.updateById(user);
        if (!success) {
            throw new CustomerException(MessageConstants.MSG_UPDATE_FAILED);
        }

        // 清理缓存，使其立即下线
        redisUtil.delete(RedisConstants.REDIS_USER_INFO_KEY + currentUser.getId());
        if (StrUtil.isNotBlank(token)) {
            authService.logout(token);
        }
    }

}