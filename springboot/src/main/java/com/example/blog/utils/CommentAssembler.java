package com.example.blog.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.entity.Article;
import com.example.blog.entity.Comment;
import com.example.blog.entity.User;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.mapper.UserMapper;
import com.example.blog.vo.user.UserVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论附加信息查询工具类
 * 功能：批量查询评论关联的作者昵称/文章标题/被回复的用户昵称等信息，返回多个语义化Map，避免循环SQL
 * 核心规范：1. 批量查询优先；2. 实体纯净不修改；3. 空值兜底避免NPE
 */
@Component
public class CommentAssembler {

    @Resource
    private UserMapper userMapper;
    @Resource
    private ArticleMapper articleMapper;
    @Resource
    private CommentMapper commentMapper;

    /**
     * 批量获取评论关联的附加信息（作者、文章）
     *
     * @param comments 评论实体列表，为空时返回空Map兜底，非空时批量查询关联信息
     * @return Map<String, Object> 结构化附加信息Map，key及对应值说明：
     * <ul>
     * <li>userIdToUserMap：Map<Long, UserVO> - 用户ID -> UserVO(包含昵称/头像)</li>
     * <li>articleIdToTitleMap：Map<Long, String> - 文章ID映射文章名称</li>
     * <li>replyCommentIdToReplyContent：Map<Long, String> - 被回复的评论ID映射被回复的评论内容摘要</li>
     * </ul>
     */
    public Map<String, Object> batchQueryCommentExtraMaps(List<Comment> comments) {
        // 最终返回的Map（存放多个子Map）
        Map<String, Object> resultMap = new HashMap<>();

        // 空值兜底
        if (CollUtil.isEmpty(comments)) {
            resultMap.put("userIdToUserMap", new HashMap<Integer, UserVO>());
            resultMap.put("articleIdToTitleMap", new HashMap<Integer, String>());
            resultMap.put("replyCommentIdToReplyContentMap", new HashMap<Integer, String>());
            return resultMap;
        }

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
            // 逻辑：如果 replyCommentId > 0，说明是回复子评论
            if (isValidId(comment.getReplyCommentId())) {
                targetCommentIds.add(comment.getReplyCommentId());
            }
            // 否则，如果 parentId > 0，说明是回复顶级评论 (需要获取顶级评论的内容)
            else if (isValidId(comment.getParentId())) {
                targetCommentIds.add(comment.getParentId());
            }
        }

        // 批量查询用户 → UserVO（包含nickname/avatar）
        Map<Long, UserVO> userIdToUserMap = new HashMap<>();
        if (CollUtil.isNotEmpty(allUserIds)) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .in(User::getId, allUserIds)
                    .select(User::getId, User::getNickname, User::getAvatar));
            userIdToUserMap = users.stream()
                    .collect(Collectors.toMap(
                            User::getId,
                            // Value Mapper: 直接 new VO
                            user -> UserVO.builder()
                                    .id(user.getId())
                                    .nickname(user.getNickname())
                                    .avatar(user.getAvatar())
                                    .build(),
                            (k1, k2) -> k1
                    ));
        }

        // 批量查询文章 → 文章标题
        Map<Long, String> articleIdToTitleMap = new HashMap<>();
        if (CollUtil.isNotEmpty(articleIds)) {
            List<Article> articles = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                    .in(Article::getId, articleIds)
                    .select(Article::getId, Article::getTitle));
            articleIdToTitleMap = articles.stream()
                    .collect(Collectors.toMap(Article::getId, Article::getTitle));
        }

        // 批量查询被回复的评论ID → 被回复的评论内容
        Map<Long, String> replyCommentIdToReplyContentMap = new HashMap<>();
        if (CollUtil.isNotEmpty(targetCommentIds)) {
            List<Comment> replyComments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                    .in(Comment::getId, targetCommentIds)
                    .select(Comment::getId, Comment::getContent));

            replyCommentIdToReplyContentMap = replyComments.stream()
                    .collect(Collectors.toMap(
                            Comment::getId,
                            // 摘要截取前20字
                            c -> StrUtil.maxLength(c.getContent(), 20)
                    ));
        }

        resultMap.put("userIdToUserMap", userIdToUserMap);
        resultMap.put("articleIdToTitleMap", articleIdToTitleMap);
        resultMap.put("replyCommentIdToReplyContentMap", replyCommentIdToReplyContentMap);

        return resultMap;
    }

    /**
     * 查询单条评论的关联附加信息（作者、文章）
     * 这是一个便利方法，内部调用批量查询逻辑，保持代码复用
     *
     * @param comment 单条评论实体
     * @return Map<String, Object> 结构化附加信息Map
     */
    public Map<String, Object> queryArticleExtraMaps(Comment comment) {
        if (comment == null) {
            return new HashMap<>();
        }
        // 复用核心逻辑：把单条包装成 List 调用
        return batchQueryCommentExtraMaps(Collections.singletonList(comment));
    }

    /**
     * 辅助方法：判断ID是否有效 (非空且大于0)
     */
    private boolean isValidId(Long id) {
        return id != null && id > 0;
    }
}
