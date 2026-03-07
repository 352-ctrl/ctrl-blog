package com.example.blog.modules.operation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.modules.operation.model.dto.AdminCommentQueryDTO;
import com.example.blog.modules.operation.model.dto.CommentAddDTO;
import com.example.blog.modules.operation.model.dto.CommentQueryDTO;
import com.example.blog.modules.operation.model.entity.Comment;
import com.example.blog.modules.operation.model.vo.AdminCommentVO;
import com.example.blog.modules.operation.model.vo.CommentVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论业务服务接口
 * 定义评论相关的业务操作方法 (作为评论领域的聚合根统一对外暴露接口)
 */
public interface CommentService extends IService<Comment> {

    /**
     * 前台分页查询评论
     *
     * @param commentQueryDTO 查询条件DTO
     * @return 分页结果
     */
    IPage<CommentVO> pageComments(CommentQueryDTO commentQueryDTO);

    /**
     * 后台分页查询评论
     *
     * @param adminCommentQueryDTO 查询条件DTO
     * @return 分页结果
     */
    IPage<AdminCommentVO> pageAdminComments(AdminCommentQueryDTO adminCommentQueryDTO);

    /**
     * 保存评论（包含重复校验和默认值设置）
     *
     * @param commentAddDTO 评论DTO
     */
    void addComment(CommentAddDTO commentAddDTO);

    /**
     * 删除评论
     *
     * @param id 评论ID
     */
    void deleteCommentById(Long id);

    /**
     * 批量删除评论
     *
     * @param ids 评论ID列表
     */
    void batchDeleteComments(List<Long> ids);

    /**
     * 计算某条评论在文章中的所在页码，用于精准跳转
     *
     * @param commentId 目标评论ID
     * @param pageSize  当前前端评论列表的每页条数
     * @return 该评论所在的页码 (默认返回 1)
     */
    Integer getCommentLocatorPage(Long commentId, Integer pageSize);

    /**
     * 清理回收站中过期的评论（包含级联清理关联数据）
     * @param recycleLimitDate 过期时间阈值
     * @return 删除的条数
     */
    int clearCommentTrash(LocalDateTime recycleLimitDate);

    /**
     * 根据文章 ID 列表，彻底清理相关的所有评论数据及附加属性 (如评论点赞等)
     * 用于文章彻底删除时，级联清理评论领域的数据，保证单一职责原则。
     *
     * @param articleIds 被清理的文章ID列表
     */
    void clearCommentsByArticleIds(List<Integer> articleIds);

    /**
     * 评论点赞业务聚合操作 (调度子表记录、增加计数、发送消息)
     *
     * @param commentId 评论ID
     * @return 最新的点赞数
     */
    Long likeComment(Long commentId);

    /**
     * 取消评论点赞业务聚合操作 (清理子表记录、减少计数)
     *
     * @param commentId 评论ID
     * @return 最新的点赞数
     */
    Long cancelLikeComment(Long commentId);

}