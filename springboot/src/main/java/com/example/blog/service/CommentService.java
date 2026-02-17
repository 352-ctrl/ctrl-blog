package com.example.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.dto.comment.AdminCommentQueryDTO;
import com.example.blog.dto.comment.CommentAddDTO;
import com.example.blog.dto.comment.CommentQueryDTO;
import com.example.blog.entity.Comment;
import com.example.blog.vo.comment.AdminCommentVO;
import com.example.blog.vo.comment.CommentVO;

import java.util.List;

/**
 * 评论业务服务接口
 * 定义评论相关的业务操作方法
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

}