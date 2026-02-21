package com.example.blog.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.convert.CommentConvert;
import com.example.blog.dto.comment.AdminCommentQueryDTO;
import com.example.blog.dto.comment.CommentAddDTO;
import com.example.blog.dto.comment.CommentQueryDTO;
import com.example.blog.dto.user.UserPayloadDTO;
import com.example.blog.entity.Article;
import com.example.blog.entity.Comment;
import com.example.blog.entity.User;
import com.example.blog.exception.CustomerException;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.service.ArticleService;
import com.example.blog.service.CommentLikeService;
import com.example.blog.service.CommentService;
import com.example.blog.service.UserService;
import com.example.blog.utils.CommentAssembler;
import com.example.blog.utils.RedisUtil;
import com.example.blog.utils.UserContext;
import com.example.blog.vo.comment.AdminCommentVO;
import com.example.blog.vo.comment.CommentVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统用户业务服务实现类
 * 实现用户相关的具体业务逻辑
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Resource
    private UserService userService;

    @Resource
    private ArticleService articleService;

    @Resource
    private CommentLikeService commentLikeService;

    @Resource
    private CommentAssembler commentAssembler;

    @Resource
    private CommentConvert commentConvert;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 批量获取评论关联的附加信息（用户、文章）
     *
     * @param comments 评论实体列表，为空时返回空Map兜底，非空时批量查询关联信息
     * @return Map<String, Object> 结构化附加信息Map，key及对应值说明：
     * <ul>
     * <li>userIdToUserMap：Map<Integer, User> - 用户ID映射包含昵称/头像/昵称的User对象</li>
     * <li>articleIdToTitleMap: Map<Integer, String> - 文章ID映射文章标题</li>
     * <li>replyCommentIdToReplyContent：Map<Integer, String> - 被回复的评论ID映射被回复的评论内容摘要</li>
     * </ul>
     */
    public Map<String, Object> getCommentExtraInfo(List<Comment> comments) {
        return commentAssembler.batchQueryCommentExtraMaps(comments);
    }

    /**
     * 根据昵称查询用户ID列表
     */
    private List<Long> getUserIdListByNickname(String userNickname) {
        if (StrUtil.isBlank(userNickname)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(User::getNickname, userNickname);
        queryWrapper.select(User::getId); // 只查询ID，减少数据量
        queryWrapper.last("LIMIT 100");
        return userService.list(queryWrapper)
                .stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }

    /**
     * 根据标题查询文章ID列表
     */
    private List<Long> getArticleIdListByTitle(String articleTitle) {
        if (StrUtil.isBlank(articleTitle)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Article::getTitle, articleTitle);
        queryWrapper.select(Article::getId); // 只查询ID，减少数据量
        queryWrapper.last("LIMIT 100");
        return articleService.list(queryWrapper)
                .stream()
                .map(Article::getId)
                .collect(Collectors.toList());
    }

    /**
     * 填充点赞状态
     */
    private void fillLikeStatus(List<CommentVO> vos) {
        if (CollUtil.isEmpty(vos)) return;

        // 提取所有评论ID
        List<Long> allIds = vos.stream().map(CommentVO::getId).toList();

        // 调用您要求的 listLikedCommentIds 获取当前用户已点赞的 ID 列表
        // 内部已处理未登录返回空集合的逻辑
        List<Long> likedIds = commentLikeService.listLikedCommentIds(allIds);

        if (CollUtil.isNotEmpty(likedIds)) {
            Set<Long> likedIdSet = new HashSet<>(likedIds);
            vos.forEach(vo -> {
                if (likedIdSet.contains(vo.getId())) {
                    vo.setLiked(true);
                }
            });
        }
    }

    /**
     * 组装树形结构并封装结果
     */
    private IPage<CommentVO> buildCommentTree(List<CommentVO> allVOs, List<Long> parentIds, Page<Comment> page) {
        Set<Long> parentIdSet = new HashSet<>(parentIds);
        List<CommentVO> rootVOs = new ArrayList<>();
        List<CommentVO> childrenVOs = new ArrayList<>();

        // 分离父子
        for (CommentVO vo : allVOs) {
            if (parentIdSet.contains(vo.getId())) {
                rootVOs.add(vo);
            } else {
                childrenVOs.add(vo);
            }
        }

        // 子评论按 parentId 分组
        Map<Long, List<CommentVO>> childrenMap = childrenVOs.stream()
                .collect(Collectors.groupingBy(CommentVO::getParentId));

        // 填充树结构
        for (CommentVO root : rootVOs) {
            List<CommentVO> myChildren = childrenMap.getOrDefault(root.getId(), new ArrayList<>());
            myChildren.sort(Comparator.comparing(CommentVO::getCreateTime));
            root.setChildren(myChildren);
            root.setReplyCount(myChildren.size());
        }

        // 封装分页结果
        Page<CommentVO> resultPage = new Page<>();
        resultPage.setCurrent(page.getCurrent());
        resultPage.setSize(page.getSize());
        resultPage.setTotal(page.getTotal());
        resultPage.setRecords(rootVOs);
        resultPage.setPages(page.getPages());
        return resultPage;
    }

    /**
     * 前台分页查询评论
     *
     * @param queryDTO 查询条件DTO
     * @return 分页结果
     */
    @Override
    public IPage<CommentVO> pageComments(CommentQueryDTO queryDTO) {
        Page<Comment> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Comment> parentQuery = new LambdaQueryWrapper<>();

        // 基础查询条件：指定文章、仅查顶级评论
        parentQuery.eq(Comment::getArticleId, queryDTO.getArticleId())
                .eq(Comment::getParentId, Constants.COMMENT_ROOT_PARENT_ID);

        // 根据前端传入的 sortType 动态决定排序方式
        if (BizStatus.CommentSort.HOTTEST.getValue().equals(queryDTO.getSortType())) {
            // 最热：按点赞数倒序，点赞数相同的按时间倒序
            parentQuery.orderByDesc(Comment::getLikeCount, Comment::getCreateTime);
        } else {
            // 最新（默认）：仅按时间倒序
            parentQuery.orderByDesc(Comment::getCreateTime);
        }

        Page<Comment> parentPage = this.page(page, parentQuery);
        if (CollUtil.isEmpty(parentPage.getRecords())) {
            return parentPage.convert(c -> null);
        }

        // 查询子评论
        List<Long> parentIds = parentPage.getRecords().stream().map(Comment::getId).toList();
        List<Comment> childComments = new ArrayList<>();
        if (CollUtil.isNotEmpty(parentIds)) {
            LambdaQueryWrapper<Comment> childQuery = new LambdaQueryWrapper<>();
            childQuery.in(Comment::getParentId, parentIds)
                    .orderByAsc(Comment::getCreateTime);
            childComments = this.list(childQuery);
        }

        // 合并数据处理
        List<Comment> allComments = new ArrayList<>(parentPage.getRecords());
        allComments.addAll(childComments);

        // 转换 VO
        Map<String, Object> extraMaps = getCommentExtraInfo(allComments);
        List<CommentVO> allCommentVOs = commentConvert.entitiesToFrontVos(allComments, extraMaps);

        // 处理点赞状态回填
        fillLikeStatus(allCommentVOs);

        // 组装树形结构
        return buildCommentTree(allCommentVOs, parentIds, page);
    }

    /**
     * 后台分页查询评论
     *
     * @param queryDTO 查询条件DTO
     * @return 分页结果
     */
    @Override
    public IPage<AdminCommentVO> pageAdminComments(AdminCommentQueryDTO queryDTO) {
        Page<Comment> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        // 用户昵称和文章标题模糊查询
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

        Page<Comment> commentPage = this.page(page, queryWrapper);
        List<Comment> records = commentPage.getRecords();

        if (CollUtil.isEmpty(records)) {
            return commentPage.convert(c -> null);
        }

        // 获取基础信息 (用户、文章)
        Map<String, Object> extraMaps = getCommentExtraInfo(records);
        List<AdminCommentVO> adminVos = commentConvert.entitiesToAdminVos(records, extraMaps);

        // 返回列表
        Page<AdminCommentVO> resultPage = new Page<>();
        resultPage.setCurrent(page.getCurrent());
        resultPage.setSize(page.getSize());
        resultPage.setTotal(page.getTotal());
        resultPage.setRecords(adminVos);
        resultPage.setPages(page.getPages());

        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addComment(CommentAddDTO addDTO) {
        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        Comment comment = commentConvert.addDtoToEntity(addDTO);
        comment.setUserId(currentUser.getId());

        Long parentId = addDTO.getParentId() == null ? Constants.COMMENT_ROOT_PARENT_ID : addDTO.getParentId();
        comment.setParentId(parentId);

        if (!parentId.equals(Constants.COMMENT_ROOT_PARENT_ID)) {
            Comment parentComment = this.getById(parentId);
            if (parentComment == null) {
                throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_COMMENT_NOT_EXIST);
            }
            // 如果父评论也是子评论（说明当前是回复子评论）
            if (Objects.equals(parentComment.getParentId(), Constants.COMMENT_ROOT_PARENT_ID)) {
                // 情况1：回复顶级评论
                comment.setParentId(parentComment.getId());
                comment.setReplyUserId(parentComment.getUserId());
            } else {
                // 情况2：回复子评论（盖楼），需要扁平化到第二层
                User repliedUser = userService.getById(parentComment.getUserId());
                if (repliedUser == null) {
                    throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_USER_NOT_EXIST);
                }
                comment.setParentId(parentComment.getParentId()); // 挂在顶级评论下
                comment.setReplyCommentId(parentComment.getId()); // 指向具体回复的子评论
                comment.setReplyUserId(parentComment.getUserId()); // 指向具体被回复的人
            }
        }

        this.save(comment);

        String articleCacheKey = RedisConstants.REDIS_ARTICLE_DETAIL_PREFIX + comment.getArticleId();
        redisUtil.delete(articleCacheKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommentById(Long id) {
        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }
        Comment comment = this.getById(id);
        if (comment == null) {
            return;
        }
        // 判断是否是作者
        boolean isOwner = comment.getUserId().equals(currentUser.getId());
        // 判断是否是管理员
        boolean isAdmin = (currentUser.getRole() == BizStatus.Role.ADMIN);
        // 权限校验：既不是作者，也不是管理员
        if (!isOwner && !isAdmin) {
            throw new CustomerException(ResultCode.FORBIDDEN, MessageConstants.MSG_NO_PERMISSION);
        }
        // 级联删除逻辑
        deleteChildrenComments(Collections.singletonList(id));
        this.lambdaUpdate()
                .eq(Comment::getId, id)
                .set(Comment::getIsDeleted, 1)
                .set(Comment::getDeleteTime, LocalDateTime.now())
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteComments(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_PARAM_ERROR);
        }
        boolean success = this.lambdaUpdate()
                .in(Comment::getId, ids)
                .set(Comment::getIsDeleted, 1)
                .set(Comment::getDeleteTime, LocalDateTime.now())
                .update();

        // 级联删除逻辑
        deleteChildrenComments(ids);
        if (!success) {
            throw new CustomerException(MessageConstants.MSG_BATCH_DELETE_FAILED);
        }
    }

    /**
     * 封装级联删除子评论的逻辑
     */
    private void deleteChildrenComments(List<Long> potentialParentIds) {
        // 只有当删除的是父评论(parentId=0)时，才需要删子评论
        // 这里为了性能，直接查找库里这些ID作为ParentId的记录进行删除
        // 如果业务逻辑严格，可以先查 selectedIds 中哪些是 parentId=0 的
        this.lambdaUpdate()
                .in(Comment::getParentId, potentialParentIds)
                .set(Comment::getIsDeleted, 1)
                .set(Comment::getDeleteTime, LocalDateTime.now())
                .update();
    }

}
