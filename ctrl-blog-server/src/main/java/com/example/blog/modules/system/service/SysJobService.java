package com.example.blog.modules.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.modules.system.model.dto.SysJobAddDTO;
import com.example.blog.modules.system.model.dto.SysJobQueryDTO;
import com.example.blog.modules.system.model.dto.SysJobUpdateDTO;
import com.example.blog.modules.system.model.entity.SysJob;
import com.example.blog.modules.system.model.vo.SysJobVO;

import java.util.List;

/**
 * 定时任务调度 Service 接口
 */
public interface SysJobService extends IService<SysJob> {

    /**
     * 新增任务
     */
    void addJob(SysJobAddDTO addDTO);

    /**
     * 更新任务
     */
    void updateJob(SysJobUpdateDTO updateDTO);

    /**
     * 删除任务
     *
     * @param id 任务ID
     */
    void deleteJobById(Long id);

    /**
     * 批量删除任务
     */
    void batchDeleteJobs(List<Long> ids);

    /**
     * 立即运行一次任务
     */
    void run(Long id);

    /**
     * 修改任务状态 (暂停/恢复)
     *
     * @param id 任务ID
     * @param status 任务状态
     */
    void changeStatus(Long id, Integer status);

    /**
     * 获取单条任务详情
     *
     * @param id 任务ID
     * @return 任务VO
     */
    SysJobVO getJobById(Long id);

    /**
     * 分页查询任务
     *
     * @param queryDTO 查询条件DTO
     * @return 分页结果
     */
    IPage<SysJobVO> pageAdminJobs(SysJobQueryDTO queryDTO);

}
