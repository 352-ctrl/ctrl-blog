package com.example.blog.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.entity.ArticleTag;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.ArticleTagMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.mapper.SysLogMapper;
import com.example.blog.service.ArticleService;
import com.example.blog.service.VisitService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 博客业务定时任务集合
 */
@Slf4j
@Component("blogTask")
public class BlogTask {

    @Resource
    private VisitService visitService;

    @Resource
    private ArticleService articleService;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private ArticleTagMapper articleTagMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private SysLogMapper sysLogMapper;

    @Value("${blog.task.recycle-bin-days:30}")
    private Integer recycleBinDays;

    @Value("${blog.task.log-retention-days:7}")
    private Integer logRetentionDays;

    /**
     * 任务方法1：同步网站访问量 (Redis -> DB)
     * 无参方法
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
     * 无参方法
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
     * 任务方法3：清理回收站 (物理删除30天前逻辑删除的数据)
     * 建议每天凌晨 3:00 执行
     */
    @Transactional(rollbackFor = Exception.class)
    public void clearTrash() {
        log.info("=== 开始执行每日系统维护任务 ===");

        // 计算 30 天前的时间点
        LocalDateTime recycleLimitDate = LocalDateTime.now().minusDays(recycleBinDays);

        // --- 子任务 A: 清理文章 ---
        try {
            List<Integer> expiredIds = articleMapper.selectExpiredArticleIds(recycleLimitDate);
            if (expiredIds != null && !expiredIds.isEmpty()) {
                log.info("发现过期文章 {} 篇，准备清理...", expiredIds.size());
                // 级联删除标签关联
                articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getArticleId, expiredIds));
                // 级联删除评论
                commentMapper.physicalDeleteByArticleIds(expiredIds);
                // 删除文章本体
                int count = articleMapper.physicalDeleteBatchIds(expiredIds);
                log.info("[维护] 成功物理删除文章及关联数据，文章数: {}", count);
            } else {
                log.info("[维护] 暂无过期的文章需要清理");
            }
        } catch (Exception e) {
            log.error("[文章清理] 异常，事务回滚", e);
            throw new RuntimeException("清理文章回收站失败", e);
        }

        // --- 子任务 B: 清理评论 ---
        try {
            int count = commentMapper.deleteExpiredTrash(recycleLimitDate);
            if (count > 0) log.info("[维护] 物理删除旧评论: {} 条", count);
        } catch (Exception e) {
            log.error("[维护] 清理评论失败", e);
        }

        // 计算 7 天前的时间点
        LocalDateTime logLimitDate = LocalDateTime.now().minusDays(logRetentionDays);

        // --- 子任务 C: 清理系统操作日志 ---
        try {
            int count = sysLogMapper.deleteExpiredLogs(logLimitDate);
            if (count > 0) log.info("[维护] 物理删除旧日志: {} 条", count);
        } catch (Exception e) {
            log.error("[维护] 清理日志失败", e);
        }

        log.info("=== 每日系统维护任务结束 ===");
    }
}
