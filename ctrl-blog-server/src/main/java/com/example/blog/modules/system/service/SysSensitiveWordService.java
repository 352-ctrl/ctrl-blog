package com.example.blog.modules.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.modules.system.model.dto.SysSensitiveWordAddDTO;
import com.example.blog.modules.system.model.dto.SysSensitiveWordQueryDTO;
import com.example.blog.modules.system.model.dto.SysSensitiveWordUpdateDTO;
import com.example.blog.modules.system.model.entity.SysSensitiveWord;
import com.example.blog.modules.system.model.vo.SysSensitiveWordVO;

import java.util.List;

/**
 * 敏感词业务服务接口
 * 定义系统后台敏感词相关的业务操作方法
 */
public interface SysSensitiveWordService extends IService<SysSensitiveWord> {

    /**
     * 获取单条敏感词详情
     *
     * @param id 敏感词ID
     * @return 敏感词VO
     */
    SysSensitiveWordVO getSensitiveWordById(Long id);

    /**
     * 分页查询敏感词
     *
     * @param queryDTO 查询条件DTO
     * @return 分页结果
     */
    IPage<SysSensitiveWordVO> pageAdminSensitiveWords(SysSensitiveWordQueryDTO queryDTO);

    /**
     * 新增敏感词
     *
     * @param addDTO 新增DTO
     */
    void addSensitiveWord(SysSensitiveWordAddDTO addDTO);

    /**
     * 更新敏感词
     *
     * @param updateDTO 更新DTO
     */
    void updateSensitiveWord(SysSensitiveWordUpdateDTO updateDTO);

    /**
     * 删除敏感词
     *
     * @param id 敏感词ID
     */
    void deleteSensitiveWordById(Long id);

    /**
     * 批量删除敏感词
     *
     * @param ids 敏感词ID列表
     */
    void batchDeleteSensitiveWords(List<Long> ids);

}