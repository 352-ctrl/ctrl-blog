package com.example.blog.modules.operation.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.modules.operation.model.bo.CommentExtraContext;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.operation.model.convert.CommentConvert;
import com.example.blog.modules.operation.model.dto.AdminCommentQueryDTO;
import com.example.blog.modules.operation.model.dto.CommentQueryDTO;
import com.example.blog.modules.user.model.dto.UserPayloadDTO;
import com.example.blog.modules.article.model.entity.Article;
import com.example.blog.modules.operation.model.entity.Comment;
import com.example.blog.modules.user.model.entity.User;
import com.example.blog.modules.operation.builder.CommentContextBuilder;
import com.example.blog.modules.article.mapper.ArticleMapper;
import com.example.blog.modules.operation.mapper.CommentMapper;
import com.example.blog.modules.operation.service.CommentLikeService;
import com.example.blog.modules.user.service.UserService;
import com.example.blog.core.security.UserContext;
import com.example.blog.modules.operation.model.vo.AdminCommentVO;
import com.example.blog.modules.operation.model.vo.CommentVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论查询管理器 (CQRS - Query侧)
 * 专门负责前后台所有复杂的评论树形组装、分页及关联数据查询
 */
@Component
public class CommentQueryManager {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private UserService userService;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private CommentLikeService commentLikeService;

    @Resource
    private CommentContextBuilder commentContextBuilder;

    @Resource
    private CommentConvert commentConvert;

    private List<Long> getUserIdListByNickname(String userNickname) {
        if (StrUtil.isBlank(userNickname)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(User::getNickname, userNickname);
        queryWrapper.select(User::getId);
        queryWrapper.last("LIMIT 100");
        return userService.list(queryWrapper)
                .stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }

    private List<Long> getArticleIdListByTitle(String articleTitle) {
        if (StrUtil.isBlank(articleTitle)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Article::getTitle, articleTitle);
        queryWrapper.select(Article::getId);
        queryWrapper.last("LIMIT 100");
        return articleMapper.selectList(queryWrapper)
                .stream()
                .map(Article::getId)
                .collect(Collectors.toList());
    }

    private void fillLikeStatus(List<CommentVO> vos) {
        if (CollUtil.isEmpty(vos)) return;

        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null) return;

        List<Long> allIds = vos.stream().map(CommentVO::getId).toList();
        List<Long> likedIds = commentLikeService.listLikedCommentIds(allIds, currentUser.getId());

        if (CollUtil.isNotEmpty(likedIds)) {
            Set<Long> likedIdSet = new HashSet<>(likedIds);
            vos.forEach(vo -> {
                if (likedIdSet.contains(vo.getId())) {
                    vo.setLiked(true);
                }
            });
        }
    }

    private IPage<CommentVO> buildCommentTree(List<CommentVO> allVOs, List<Long> parentIds, Page<Comment> page) {
        if (CollUtil.isEmpty(allVOs)) {
            Page<CommentVO> emptyPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
            emptyPage.setRecords(Collections.emptyList());
            return emptyPage;
        }

        Set<Long> parentIdSet = new HashSet<>(parentIds);
        List<CommentVO> rootVOs = new ArrayList<>();
        List<CommentVO> childrenVOs = new ArrayList<>();

        for (CommentVO vo : allVOs) {
            if (parentIdSet.contains(vo.getId())) {
                rootVOs.add(vo);
            } else {
                childrenVOs.add(vo);
            }
        }

        Map<Long, List<CommentVO>> childrenMap = childrenVOs.stream()
                .collect(Collectors.groupingBy(CommentVO::getParentId));

        for (CommentVO root : rootVOs) {
            List<CommentVO> myChildren = childrenMap.getOrDefault(root.getId(), new ArrayList<>());
            myChildren.sort(Comparator.comparing(CommentVO::getCreateTime));
            root.setChildren(myChildren);
            root.setReplyCount(myChildren.size());
        }

        Page<CommentVO> resultPage = new Page<>();
        resultPage.setCurrent(page.getCurrent());
        resultPage.setSize(page.getSize());
        resultPage.setTotal(page.getTotal());
        resultPage.setRecords(rootVOs);
        resultPage.setPages(page.getPages());
        return resultPage;
    }

    public IPage<CommentVO> pageComments(CommentQueryDTO queryDTO) {
        Assert.notNull(queryDTO, "查询条件不能为空");
        Assert.notNull(queryDTO.getArticleId(), "文章ID不能为空");

        Page<Comment> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Comment> parentQuery = new LambdaQueryWrapper<>();

        parentQuery.eq(Comment::getArticleId, queryDTO.getArticleId())
                .eq(Comment::getParentId, Constants.COMMENT_ROOT_PARENT_ID);

        if (BizStatus.CommentSort.HOTTEST.getValue().equals(queryDTO.getSortType())) {
            parentQuery.orderByDesc(Comment::getLikeCount, Comment::getCreateTime);
        } else {
            parentQuery.orderByDesc(Comment::getCreateTime);
        }

        Page<Comment> parentPage = commentMapper.selectPage(page, parentQuery);
        if (CollUtil.isEmpty(parentPage.getRecords())) {
            return parentPage.convert(c -> null);
        }

        List<Long> parentIds = parentPage.getRecords().stream().map(Comment::getId).toList();
        List<Comment> childComments = new ArrayList<>();
        if (CollUtil.isNotEmpty(parentIds)) {
            LambdaQueryWrapper<Comment> childQuery = new LambdaQueryWrapper<>();
            childQuery.in(Comment::getParentId, parentIds)
                    .orderByAsc(Comment::getCreateTime);
            childComments = commentMapper.selectList(childQuery);
        }

        List<Comment> allComments = new ArrayList<>(parentPage.getRecords());
        allComments.addAll(childComments);

        CommentExtraContext context = commentContextBuilder.buildExtraContext(allComments);
        List<CommentVO> allCommentVOs = commentConvert.entitiesToFrontVos(allComments, context);

        fillLikeStatus(allCommentVOs);

        return buildCommentTree(allCommentVOs, parentIds, page);
    }

    public IPage<AdminCommentVO> pageAdminComments(AdminCommentQueryDTO queryDTO) {
        Assert.notNull(queryDTO, "查询条件不能为空");

        Page<Comment> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();

        String userNickname = queryDTO.getUserNickname();
        String articleTitle = queryDTO.getArticleTitle();

        if (StrUtil.isNotBlank(userNickname)) {
            List<Long> userIds = getUserIdListByNickname(userNickname);
            if (userIds.isEmpty()) {
                return page.convert(comment -> null);
            }
            queryWrapper.in(Comment::getUserId, userIds);
        }

        if (StrUtil.isNotBlank(articleTitle)) {
            List<Long> articleIds = getArticleIdListByTitle(articleTitle);
            if (articleIds.isEmpty()) {
                return page.convert(comment -> null);
            }
            queryWrapper.in(Comment::getArticleId, articleIds);
        }

        queryWrapper.orderByDesc(Comment::getCreateTime);

        Page<Comment> commentPage = commentMapper.selectPage(page, queryWrapper);
        List<Comment> records = commentPage.getRecords();

        if (CollUtil.isEmpty(records)) {
            return commentPage.convert(c -> null);
        }

        CommentExtraContext context = commentContextBuilder.buildExtraContext(records);
        List<AdminCommentVO> adminVos = commentConvert.entitiesToAdminVos(records, context);

        Page<AdminCommentVO> resultPage = new Page<>();
        resultPage.setCurrent(page.getCurrent());
        resultPage.setSize(page.getSize());
        resultPage.setTotal(page.getTotal());
        resultPage.setRecords(adminVos);
        resultPage.setPages(page.getPages());

        return resultPage;
    }

    public Integer getCommentLocatorPage(Long commentId, Integer pageSize) {
        if (commentId == null || pageSize == null || pageSize <= 0) {
            return 1;
        }

        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return 1;
        }

        Long rootId = comment.getParentId().equals(Constants.COMMENT_ROOT_PARENT_ID)
                ? comment.getId()
                : comment.getParentId();

        Comment rootComment = commentMapper.selectById(rootId);
        if (rootComment == null) {
            return 1;
        }

        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getArticleId, rootComment.getArticleId())
                .eq(Comment::getParentId, Constants.COMMENT_ROOT_PARENT_ID)
                .gt(Comment::getCreateTime, rootComment.getCreateTime());

        long countAhead = commentMapper.selectCount(queryWrapper);

        return (int) (countAhead / pageSize) + 1;
    }
}