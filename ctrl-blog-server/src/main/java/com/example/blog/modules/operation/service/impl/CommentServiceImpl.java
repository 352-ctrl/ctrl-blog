package com.example.blog.modules.operation.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.modules.operation.model.convert.CommentConvert;
import com.example.blog.modules.operation.model.dto.AdminCommentQueryDTO;
import com.example.blog.modules.operation.model.dto.CommentAddDTO;
import com.example.blog.modules.operation.model.dto.CommentQueryDTO;
import com.example.blog.modules.user.model.dto.UserPayloadDTO;
import com.example.blog.modules.article.model.entity.Article;
import com.example.blog.modules.operation.model.entity.Comment;
import com.example.blog.modules.operation.model.entity.CommentLike;
import com.example.blog.modules.user.model.entity.User;
import com.example.blog.modules.operation.event.InteractiveMessageEvent;
import com.example.blog.core.exception.CustomerException;
import com.example.blog.modules.operation.manager.CommentQueryManager;
import com.example.blog.modules.article.mapper.ArticleMapper;
import com.example.blog.modules.operation.mapper.CommentMapper;
import com.example.blog.modules.operation.service.CommentLikeService;
import com.example.blog.modules.operation.service.CommentService;
import com.example.blog.modules.user.service.UserService;
import com.example.blog.common.utils.RedisUtil;
import com.example.blog.modules.system.validator.SensitiveWordManager;
import com.example.blog.core.security.UserContext;
import com.example.blog.modules.operation.model.vo.AdminCommentVO;
import com.example.blog.modules.operation.model.vo.CommentVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 评论业务服务实现类 (评论领域聚合根 - Command侧)
 * 专门负责评论的新增、删除、点赞、系统级连带处理与通知
 */
@Slf4j
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Resource
    private UserService userService;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private CommentLikeService commentLikeService;

    @Resource
    private CommentConvert commentConvert;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private SensitiveWordManager sensitiveWordManager;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Resource
    private CommentQueryManager commentQueryManager;

    /* ================= 以下为读操作，统统委托给 QueryManager ================= */

    @Override
    public IPage<CommentVO> pageComments(CommentQueryDTO queryDTO) {
        return commentQueryManager.pageComments(queryDTO);
    }

    @Override
    public IPage<AdminCommentVO> pageAdminComments(AdminCommentQueryDTO queryDTO) {
        return commentQueryManager.pageAdminComments(queryDTO);
    }

    @Override
    public Integer getCommentLocatorPage(Long commentId, Integer pageSize) {
        return commentQueryManager.getCommentLocatorPage(commentId, pageSize);
    }


    /* ================= 以下为写操作，保留在 ServiceImpl 控制核心业务与事务 ================= */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addComment(CommentAddDTO addDTO) {
        Assert.notNull(addDTO, "新增评论参数不能为空");
        UserPayloadDTO currentUser = UserContext.get();
        if (currentUser == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        Comment comment = commentConvert.addDtoToEntity(addDTO);
        comment.setUserId(currentUser.getId());

        // 过滤敏感词，将违规词替换为星号 '*'
        String originalContent = comment.getContent();
        String safeContent = sensitiveWordManager.replace(originalContent, Constants.SENSITIVE_REPLACE_CHAR);
        comment.setContent(safeContent);

        Long parentId = addDTO.getParentId() == null ? Constants.COMMENT_ROOT_PARENT_ID : addDTO.getParentId();
        comment.setParentId(parentId);

        if (!parentId.equals(Constants.COMMENT_ROOT_PARENT_ID)) {
            Comment parentComment = this.getById(parentId);
            if (parentComment == null) {
                throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_ORIGINAL_COMMENT_DELETED);
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

        // 刷新缓存
        String articleCacheKey = RedisConstants.REDIS_ARTICLE_DETAIL_PREFIX + comment.getArticleId();
        redisUtil.delete(articleCacheKey);

        // 发送评论互动消息
        if (Constants.COMMENT_ROOT_PARENT_ID.equals(parentId)) {
            // 场景 A：这是一级评论（直接评论文章） -> 发给文章作者
            Article article = articleMapper.selectById(comment.getArticleId());
            if (article != null) {
                eventPublisher.publishEvent(new InteractiveMessageEvent(
                        this,
                        article.getUserId(),
                        currentUser.getId(),
                        BizStatus.MessageType.COMMENT,
                        comment.getArticleId(),
                        BizStatus.MessageBizType.ARTICLE,
                        comment.getId(),
                        comment.getContent()
                ));
            }
        } else {
            // 场景 B：这是回复别人的评论 -> 发给被回复的那个用户
            if (comment.getReplyUserId() != null) {
                eventPublisher.publishEvent(new InteractiveMessageEvent(
                        this,
                        comment.getReplyUserId(),
                        currentUser.getId(),
                        BizStatus.MessageType.COMMENT,
                        comment.getArticleId(),
                        BizStatus.MessageBizType.COMMENT,
                        comment.getId(),
                        comment.getContent()
                ));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommentById(Long id) {
        Assert.notNull(id, "评论ID不能为空");
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
        boolean isAdmin = (currentUser.getRole() == BizStatus.Role.ADMIN || currentUser.getRole() == BizStatus.Role.SUPER_ADMIN);
        // 权限校验：既不是作者，也不是管理员
        if (!isOwner && !isAdmin) {
            throw new CustomerException(ResultCode.FORBIDDEN, MessageConstants.MSG_NO_PERMISSION);
        }
        // 级联删除逻辑
        deleteChildrenComments(Collections.singletonList(id));
        this.lambdaUpdate()
                .eq(Comment::getId, id)
                .set(Comment::getIsDeleted, BizStatus.DeleteStatus.DELETED)
                .set(Comment::getDeleteTime, LocalDateTime.now())
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteComments(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            log.warn("批量删除评论失败：传入的 ID 列表为空");
            return;
        }
        boolean success = this.lambdaUpdate()
                .in(Comment::getId, ids)
                .set(Comment::getIsDeleted, BizStatus.DeleteStatus.DELETED)
                .set(Comment::getDeleteTime, LocalDateTime.now())
                .update();

        // 级联删除逻辑
        deleteChildrenComments(ids);
        if (!success) {
            throw new CustomerException(MessageConstants.MSG_BATCH_DELETE_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long likeComment(Long commentId) {
        Assert.notNull(commentId, "评论ID不能为空");

        UserPayloadDTO user = UserContext.get();
        if (user == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        Comment comment = this.getById(commentId);
        if (comment == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_COMMENT_NOT_EXIST);
        }

        // 1. 调用子服务落盘点赞关联关系
        commentLikeService.saveCommentLike(commentId, user.getId());

        // 2. 聚合根本身更新计数 (假设你的 Mapper 里有 incrLikeCount 方法)
        this.baseMapper.incrLikeCount(commentId);

        // 3. 使用发布事件来触发消息
        eventPublisher.publishEvent(new InteractiveMessageEvent(
                this,
                comment.getUserId(),
                user.getId(),
                BizStatus.MessageType.LIKE,
                comment.getArticleId(),
                BizStatus.MessageBizType.COMMENT,
                commentId,
                comment.getContent()
        ));
        // 返回最新的点赞数
        return comment.getLikeCount() + 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long cancelLikeComment(Long commentId) {
        Assert.notNull(commentId, "评论ID不能为空");

        UserPayloadDTO user = UserContext.get();
        if (user == null) {
            throw new CustomerException(ResultCode.UNAUTHORIZED, MessageConstants.MSG_NOT_LOGIN);
        }

        // 1. 调用子服务移除关联关系
        commentLikeService.removeCommentLike(commentId, user.getId());

        // 2. 聚合根本身更新计数
        this.baseMapper.decrLikeCount(commentId);

        Comment comment = this.getById(commentId);
        return comment != null ? comment.getLikeCount() : 0L;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearCommentsByArticleIds(List<Integer> articleIds) {
        if (CollUtil.isEmpty(articleIds)) {
            return;
        }

        // 1. 先查出这些文章下所有的评论 ID
        List<Long> commentIds = this.baseMapper.selectObjs(
                new LambdaQueryWrapper<Comment>()
                        .select(Comment::getId)
                        .in(Comment::getArticleId, articleIds)
        ).stream().map(id -> Long.valueOf(id.toString())).collect(Collectors.toList());

        if (CollUtil.isNotEmpty(commentIds)) {
            // 2. 清理这些评论的点赞数据 (blog_comment_like 表)
            commentLikeService.remove(new LambdaQueryWrapper<CommentLike>()
                    .in(CommentLike::getCommentId, commentIds));

            // 如果评论还有“举报记录”、“回复的回复”等其他子数据，都在这里一并清理
        }

        // 3. 物理删除评论本体 (blog_comment 表)
        this.baseMapper.physicalDeleteByArticleIds(articleIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int clearCommentTrash(LocalDateTime msgLimitDate) {
        if (msgLimitDate == null) {
            return 0;
        }
        // 直接调用底层 Mapper 进行物理删除
        return this.baseMapper.physicalDeleteExpiredTrash(msgLimitDate);
    }

    /**
     * 封装级联删除子评论的逻辑 (内部写操作辅助方法)
     */
    private void deleteChildrenComments(List<Long> potentialParentIds) {
        if (CollUtil.isEmpty(potentialParentIds)) {
            return;
        }
        this.lambdaUpdate()
                .in(Comment::getParentId, potentialParentIds)
                .set(Comment::getIsDeleted, BizStatus.DeleteStatus.DELETED)
                .set(Comment::getDeleteTime, LocalDateTime.now())
                .update();
    }
}