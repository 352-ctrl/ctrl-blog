package com.example.blog.modules.operation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.modules.operation.model.dto.NoticeAddDTO;
import com.example.blog.modules.operation.model.dto.NoticeQueryDTO;
import com.example.blog.modules.operation.model.dto.NoticeUpdateDTO;
import com.example.blog.modules.operation.model.entity.Notice;
import com.example.blog.modules.operation.model.vo.AdminNoticeVO;
import com.example.blog.modules.operation.model.vo.NoticeVO;

import java.util.List;

/**
 * 系统公告业务服务接口
 * 定义系统公告相关的业务操作方法
 */
public interface NoticeService extends IService<Notice> {

    /**
     * 获取首页公告（置顶+最新）
     *
     * @return 公告列表
     */
    List<NoticeVO> listScrollNotices();

    /**
     * 获取单条公告详情
     *
     * @param id 公告ID
     * @return 公告VO
     */
    AdminNoticeVO getNoticeById(Long id);

    /**
     * 分页查询公告
     *
     * @param noticeQueryDTO 查询条件DTO
     * @return 分页结果
     */
    IPage<AdminNoticeVO> pageAdminNotices(NoticeQueryDTO noticeQueryDTO);

    /**
     * 新增公告
     *
     * @param noticeAddDTO 公告DTO
     */
    void addNotice(NoticeAddDTO noticeAddDTO);

    /**
     * 更新公告
     *
     * @param noticeUpdateDTO 公告DTO
     */
    void updateNotice(NoticeUpdateDTO noticeUpdateDTO);


    /**
     * 删除公告
     *
     * @param id 公告ID
     */
    void deleteNoticeById(Long id);

    /**
     * 批量删除公告
     *
     * @param ids 公告ID列表
     */
    void batchDeleteNotices(List<Long> ids);

}