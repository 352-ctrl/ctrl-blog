package com.example.blog.modules.operation.builder;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.modules.operation.model.bo.CommentExtraContext;
import com.example.blog.modules.article.model.entity.Article;
import com.example.blog.modules.operation.model.entity.Comment;
import com.example.blog.modules.user.model.entity.User;
import com.example.blog.modules.article.mapper.ArticleMapper;
import com.example.blog.modules.operation.mapper.CommentMapper;
import com.example.blog.modules.user.mapper.UserMapper;
import com.example.blog.modules.user.model.vo.UserVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论上下文构建器
 * 职责：专职负责批量查询评论关联的作者/文章/回复内容信息，构建防 NPE 的强类型上下文对象
 */
@Component
public class CommentContextBuilder {

    @Resource
    private UserMapper userMapper;
    @Resource
    private ArticleMapper articleMapper;
    @Resource
    private CommentMapper commentMapper;

    /**
     * 批量获取评论关联的附加信息（作者、文章、回复内容）
     */
    public CommentExtraContext buildExtraContext(List<Comment> comments) {
        if (CollUtil.isEmpty(comments)) {
            return new CommentExtraContext();
        }

        CommentExtraContext context = new CommentExtraContext();

        // 1. 提取所有关联 ID
        Set<Long> allUserIds = new HashSet<>(); // 合并评论人和被评论人
        Set<Long> articleIds = new HashSet<>();
        Set<Long> targetCommentIds = new HashSet<>();

        for (Comment comment : comments) {
            // 收集用户ID (作者 + 被回复人)
            if (isValidId(comment.getUserId())) {
                allUserIds.add(comment.getUserId());
            }
            if (isValidId(comment.getReplyUserId())) {
                allUserIds.add(comment.getReplyUserId());
            }

            // 收集文章ID
            if (isValidId(comment.getArticleId())) {
                articleIds.add(comment.getArticleId());
            }

            // 收集被回复的评论ID
            if (isValidId(comment.getReplyCommentId())) {
                targetCommentIds.add(comment.getReplyCommentId());
            } else if (isValidId(comment.getParentId())) {
                targetCommentIds.add(comment.getParentId());
            }
        }

        // 批量查询用户
        if (CollUtil.isNotEmpty(allUserIds)) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .in(User::getId, allUserIds)
                    .select(User::getId, User::getNickname, User::getAvatar));

            Map<Long, UserVO> userMap = users.stream()
                    .collect(Collectors.toMap(
                            User::getId,
                            user -> UserVO.builder()
                                    .id(user.getId())
                                    .nickname(user.getNickname())
                                    .avatar(user.getAvatar())
                                    .build(),
                            (k1, k2) -> k1
                    ));
            context.setUserMap(userMap);
        }

        // 批量查询文章
        if (CollUtil.isNotEmpty(articleIds)) {
            List<Article> articles = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                    .in(Article::getId, articleIds)
                    .select(Article::getId, Article::getTitle));

            Map<Long, String> articleMap = articles.stream()
                    .collect(Collectors.toMap(Article::getId, Article::getTitle));
            context.setArticleMap(articleMap);
        }

        // 批量查询被回复的评论
        if (CollUtil.isNotEmpty(targetCommentIds)) {
            List<Comment> replyComments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                    .in(Comment::getId, targetCommentIds)
                    .select(Comment::getId, Comment::getContent));

            Map<Long, String> replyContentMap = replyComments.stream()
                    .collect(Collectors.toMap(
                            Comment::getId,
                            c -> StrUtil.maxLength(c.getContent(), 20)
                    ));
            context.setReplyContentMap(replyContentMap);
        }

        return context;
    }

    /**
     * 查询单条评论的关联附加信息
     */
    public CommentExtraContext buildExtraContext(Comment comment) {
        if (comment == null) {
            return new CommentExtraContext();
        }
        return buildExtraContext(Collections.singletonList(comment));
    }

    private boolean isValidId(Long id) {
        return id != null && id > 0;
    }
}