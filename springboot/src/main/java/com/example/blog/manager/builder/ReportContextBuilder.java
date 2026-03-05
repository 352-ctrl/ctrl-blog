package com.example.blog.manager.builder;

import cn.hutool.core.collection.CollUtil;
import com.example.blog.bo.ReportExtraContext;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.entity.Article;
import com.example.blog.entity.Comment;
import com.example.blog.entity.Report;
import com.example.blog.entity.User;
import com.example.blog.service.ArticleService;
import com.example.blog.service.CommentService;
import com.example.blog.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 举报附加信息查询工具类
 * 功能：批量查询举报关联的举报人、文章、评论、被举报用户信息
 */
@Component
public class ReportContextBuilder {

    @Resource
    private UserService userService;

    @Resource
    private ArticleService articleService;

    @Resource
    private CommentService commentService;

    public ReportExtraContext buildContext(List<Report> reports) {
        ReportExtraContext context = new ReportExtraContext();

        if (CollUtil.isEmpty(reports)) {
            return context;
        }

        Set<Long> reporterUserIds = new HashSet<>();
        Set<Long> targetArticleIds = new HashSet<>();
        Set<Long> targetCommentIds = new HashSet<>();
        Set<Long> targetUserIds = new HashSet<>();

        // 1. 提取所需 ID
        for (Report record : reports) {
            if (record.getUserId() != null) {
                reporterUserIds.add(record.getUserId());
            }

            BizStatus.ReportTargetType type = record.getTargetType();
            Long targetId = record.getTargetId();
            if (targetId != null && type != null) {
                if (BizStatus.ReportTargetType.ARTICLE.equals(type)) {
                    targetArticleIds.add(targetId);
                } else if (BizStatus.ReportTargetType.COMMENT.equals(type)) {
                    targetCommentIds.add(targetId);
                } else if (BizStatus.ReportTargetType.USER.equals(type)) {
                    targetUserIds.add(targetId);
                }
            }
        }

        // 2. 批量查询并塞入 Context
        if (CollUtil.isNotEmpty(reporterUserIds)) {
            context.setReporterMap(userService.listByIds(reporterUserIds).stream()
                    .collect(Collectors.toMap(User::getId, User::getNickname)));
        }

        if (CollUtil.isNotEmpty(targetArticleIds)) {
            context.setArticleMap(articleService.listByIds(targetArticleIds).stream()
                    .collect(Collectors.toMap(Article::getId, Article::getTitle)));
        }

        if (CollUtil.isNotEmpty(targetCommentIds)) {
            context.setCommentMap(commentService.listByIds(targetCommentIds).stream()
                    .collect(Collectors.toMap(Comment::getId, Comment::getContent)));
        }

        if (CollUtil.isNotEmpty(targetUserIds)) {
            context.setTargetUserMap(userService.listByIds(targetUserIds).stream()
                    .collect(Collectors.toMap(User::getId, User::getNickname)));
        }

        return context;
    }
}