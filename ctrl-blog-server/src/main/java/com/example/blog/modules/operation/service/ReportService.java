package com.example.blog.modules.operation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.modules.operation.model.dto.ReportAddDTO;
import com.example.blog.modules.operation.model.dto.ReportProcessDTO;
import com.example.blog.modules.operation.model.dto.ReportQueryDTO;
import com.example.blog.modules.operation.model.entity.Report;
import com.example.blog.modules.operation.model.vo.AdminReportVO;

import java.util.List;

/**
 * 内容举报业务服务接口
 */
public interface ReportService extends IService<Report> {

    /**
     * 前台：提交举报
     * @param addDTO 举报新增DTO
     */
    void addReport(ReportAddDTO addDTO);

    /**
     * 后台：分页查询举报列表
     * @param queryDTO 查询条件DTO
     * @return 组装好的VO分页对象
     */
    IPage<AdminReportVO> pageAdminReports(ReportQueryDTO queryDTO);

    /**
     * 后台：处理举报
     * @param processDTO 处理举报DTO
     */
    void processReport(ReportProcessDTO processDTO);

    /**
     * 删除单条举报记录
     *
     * @param id 举报ID
     */
    void deleteReportById(Long id);

    /**
     * 批量删除举报记录
     *
     * @param ids 举报ID列表
     */
    void batchDeleteReports(List<Long> ids);
}