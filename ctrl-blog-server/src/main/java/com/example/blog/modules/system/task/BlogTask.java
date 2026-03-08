package com.example.blog.modules.system.task;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.file.service.FileService;
import com.example.blog.modules.monitor.service.VisitService;
import com.example.blog.modules.operation.service.CommentService;
import com.example.blog.modules.operation.service.MessageService;
import com.example.blog.modules.system.service.SysLoginLogService;
import com.example.blog.modules.system.service.SysOperLogService;
import com.example.blog.modules.user.model.dto.UserPayloadDTO;
import com.example.blog.modules.article.model.entity.Article;
import com.example.blog.modules.user.model.entity.User;
import com.example.blog.modules.article.service.ArticleService;
import com.example.blog.modules.user.service.UserService;
import com.example.blog.core.security.UserContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 博客业务定时任务集合
 */
@Slf4j
@Component("blogTask")
public class BlogTask {

    @Resource
    private VisitService visitService;

    @Resource
    private UserService userService;

    @Resource
    private ArticleService articleService;

    @Resource
    private CommentService commentService;

    @Resource
    private SysOperLogService sysOperLogService;

    @Resource
    private SysLoginLogService sysLoginLogService;

    @Resource
    private MessageService messageService;

    @Resource
    private FileService fileService;

    @Value("${blog.task.recycle-bin-days:30}")
    private Integer recycleBinDays;

    @Value("${blog.task.log-retention-days:7}")
    private Integer logRetentionDays;

    @Value("${blog.task.cancel-cooling-days:7}")
    private Integer cancelCoolingDays;

    @Value("${blog.task.message-retention-days:30}")
    private Integer messageRetentionDays;

    /**
     * 任务方法1：同步网站访问量 (Redis -> DB)
     */
    public void syncSiteVisit() {
        log.info("开始执行同步任务：网站访问量 Redis -> DB");
        try {
            visitService.syncVisitDataToDb();
            log.info("同步网站访问量完成");
        } catch (Exception e) {
            log.error("同步网站访问量失败", e);
        }
    }

    /**
     * 任务方法2：同步文章阅读量 (Redis -> DB)
     */
    public void syncArticleViews() {
        log.info("开始执行同步任务：文章阅读量 Redis -> DB");
        try {
            articleService.syncArticleViewsToDb();
            log.info("同步文章阅读量完成");
        } catch (Exception e) {
            log.error("同步文章阅读量失败", e);
        }
    }

    /**
     * 任务方法3：清理回收站与系统维护
     * 建议每天凌晨 3:00 执行
     */
    public void clearTrash() {
        log.info("=== 开始执行每日系统维护任务 ===");

        // 计算 30 天前的时间点
        LocalDateTime recycleLimitDate = LocalDateTime.now().minusDays(recycleBinDays);

        // --- 子任务 A: 清理文章 ---
        try {
            int count = articleService.clearArticleTrash(recycleLimitDate);
            if (count > 0) {
                log.info("[维护] 成功物理删除文章及关联数据，文章数: {}", count);
            } else {
                log.info("[维护] 暂无过期的文章需要清理");
            }
        } catch (Exception e) {
            log.error("[维护] 清理文章回收站失败", e);
        }

        // --- 子任务 B: 清理评论 ---
        try {
            int count = commentService.clearCommentTrash(recycleLimitDate);
            if (count > 0) log.info("[维护] 物理删除旧评论: {} 条", count);
        } catch (Exception e) {
            log.error("[维护] 清理评论失败", e);
        }

        // 计算 7 天前的时间点
        LocalDateTime logLimitDate = LocalDateTime.now().minusDays(logRetentionDays);

        // --- 子任务 C: 清理系统操作日志 ---
        try {
            int count = sysOperLogService.clearOperLogTrash(logLimitDate);
            if (count > 0) log.info("[维护] 物理删除旧系统日志: {} 条", count);
        } catch (Exception e) {
            log.error("[维护] 清理系统日志失败", e);
        }

        // --- 子任务 D: 清理系统登录日志 ---
        try {
            int count = sysLoginLogService.clearLoginLogTrash(logLimitDate);
            if (count > 0) log.info("[维护] 物理删除旧登录日志: {} 条", count);
        } catch (Exception e) {
            log.error("[维护] 清理登录日志失败", e);
        }

        // --- 子任务 E: 清理系统消息通知 ---
        try {
            LocalDateTime msgLimitDate = LocalDateTime.now().minusDays(messageRetentionDays);
            int count = messageService.clearMessageTrash(msgLimitDate);
            if (count > 0) log.info("[维护] 物理删除旧系统消息: {} 条", count);
        } catch (Exception e) {
            log.error("[维护] 清理系统消息失败", e);
        }

        // --- 子任务 F: 处理到达注销冷静期的用户 ---
        try {
            LocalDateTime cancelLimitDate = LocalDateTime.now().minusDays(cancelCoolingDays);

            List<User> usersToCancel = userService.list(
                    new LambdaQueryWrapper<User>()
                            .select(User::getId)
                            .eq(User::getStatus, BizStatus.User.PENDING_DELETION)
                            .le(User::getCancelTime, cancelLimitDate)
            );

            if (usersToCancel != null && !usersToCancel.isEmpty()) {
                List<Long> userIds = usersToCancel.stream().map(User::getId).collect(Collectors.toList());
                log.info("发现 {} 个用户注销冷静期已过，准备执行数据脱敏...", userIds.size());

                // 伪造系统超级管理员上下文，防止 userService 抛出未登录异常
                UserPayloadDTO systemUser = new UserPayloadDTO();
                systemUser.setId(0L); // 虚拟的系统ID
                systemUser.setRole(BizStatus.Role.SUPER_ADMIN); // 赋予最高权限
                UserContext.set(systemUser);

                userService.batchDeleteUsers(userIds);

                log.info("[维护] 成功处理注销用户");
            } else {
                log.info("[维护] 今日无需要脱敏的注销用户");
            }
        } catch (Exception e) {
            log.error("[维护] 处理用户注销脱敏失败", e);
        } finally {
            // 在 finally 中清除上下文，防止线程池复用导致权限泄漏
            UserContext.remove();
        }

        // --- 子任务 G: 清理孤儿文件 ---
        try {
            log.info("开始扫描并清理孤儿文件...");
            // 1. 获取全站所有正在使用的文件名集合
            Set<String> activeFiles = collectAllActiveFiles();

            // 2. 传递给底层去比对和删除
            int count = fileService.clearOrphanFiles(activeFiles);
            if (count > 0) {
                log.info("[维护] 成功物理删除孤儿文件: {} 个", count);
            } else {
                log.info("[维护] 暂无孤儿文件需要清理");
            }
        } catch (Exception e) {
            log.error("[维护] 清理孤儿文件失败", e);
        }

        // --- 子任务 H: 扫描并主动解封已到期的用户 ---
        try {
            log.info("开始扫描并主动解封已到期的用户...");
            LambdaUpdateWrapper<User> unbanWrapper = new LambdaUpdateWrapper<>();
            unbanWrapper.set(User::getStatus, BizStatus.User.NORMAL)
                    .set(User::getDisableEndTime, null)
                    .set(User::getDisableReason, null)
                    .eq(User::getStatus, BizStatus.User.DISABLE)
                    .isNotNull(User::getDisableEndTime)
                    .le(User::getDisableEndTime, LocalDateTime.now()); // 到期时间 <= 当前时间

            boolean success = userService.update(unbanWrapper);
            if (success) {
                log.info("[维护] 自动解封任务执行完成！");
            } else {
                log.info("[维护] 暂无需要自动解封的用户");
            }
        } catch (Exception e) {
            log.error("[维护] 自动解封任务执行失败", e);
        }

        log.info("=== 每日系统维护任务结束 ===");
    }

    /**
     * 搜集系统中所有正在使用的文件名称
     */
    private Set<String> collectAllActiveFiles() {
        Set<String> activeFiles = new HashSet<>();

        // 1. 收集用户头像
        List<User> users = userService.list(new LambdaQueryWrapper<User>().select(User::getAvatar).isNotNull(User::getAvatar));
        users.forEach(u -> activeFiles.add(extractFileName(u.getAvatar())));

        // 2. 收集文章封面和内容中的图片
        List<Article> articles = articleService.list(new LambdaQueryWrapper<Article>().select(Article::getCover, Article::getContent));
        for (Article a : articles) {
            if (StrUtil.isNotBlank(a.getCover())) {
                activeFiles.add(extractFileName(a.getCover()));
            }
            if (StrUtil.isNotBlank(a.getContent())) {
                // 利用 Hutool 正则提取 Markdown/HTML 中的图片链接
                // 匹配 HTML src="..." 或者 Markdown (...)
                List<String> urls = ReUtil.findAll("(?<=src=\")[^\"]+(?=\")|(?<=\\()[^\\)]+(?=\\))", a.getContent(), 0);
                urls.forEach(url -> activeFiles.add(extractFileName(url)));
            }
        }

        activeFiles.remove(null);
        activeFiles.remove("");
        return activeFiles;
    }

    /**
     * 从 URL 中提取真实的文件名
     */
    private String extractFileName(String url) {
        if (StrUtil.isBlank(url)) return null;
        return FileUtil.getName(url);
    }
}