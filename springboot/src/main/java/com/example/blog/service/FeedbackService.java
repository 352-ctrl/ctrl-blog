package com.example.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.dto.feedback.FeedbackAddDTO;
import com.example.blog.dto.feedback.FeedbackProcessDTO;
import com.example.blog.dto.feedback.FeedbackQueryDTO;
import com.example.blog.entity.Feedback;
import com.example.blog.vo.AdminFeedbackVO;

import java.util.List;

/**
 * 意见反馈业务服务接口
 */
public interface FeedbackService extends IService<Feedback> {

    /**
     * 前台：提交意见反馈
     * @param addDTO 新增反馈DTO
     */
    void addFeedback(FeedbackAddDTO addDTO);

    /**
     * 后台：分页查询反馈列表
     * @param queryDTO 查询条件DTO
     * @return 组装好的VO分页对象
     */
    IPage<AdminFeedbackVO> pageAdminFeedbacks(FeedbackQueryDTO queryDTO);

    /**
     * 后台：处理反馈 (回复并改变状态)
     * @param processDTO 处理反馈DTO
     */
    void processFeedback(FeedbackProcessDTO processDTO);

    /**
     * 删除单条反馈记录
     *
     * @param id 反馈ID
     */
    void deleteFeedbackById(Long id);

    /**
     * 批量删除反馈记录
     *
     * @param ids 反馈ID列表
     */
    void batchDeleteFeedbacks(List<Long> ids);
}