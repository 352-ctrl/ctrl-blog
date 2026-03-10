package com.example.blog.modules.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.modules.system.model.convert.SysSensitiveWordConvert;
import com.example.blog.modules.system.model.dto.SysSensitiveWordAddDTO;
import com.example.blog.modules.system.model.dto.SysSensitiveWordQueryDTO;
import com.example.blog.modules.system.model.dto.SysSensitiveWordUpdateDTO;
import com.example.blog.modules.system.model.entity.SysSensitiveWord;
import com.example.blog.core.exception.CustomerException;
import com.example.blog.modules.system.mapper.SysSensitiveWordMapper;
import com.example.blog.modules.system.service.SysSensitiveWordService;
import com.example.blog.modules.system.validator.SensitiveWordManager;
import com.example.blog.modules.system.model.vo.SysSensitiveWordVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 敏感词业务服务实现类
 * 实现敏感词相关的具体业务逻辑
 */
@Slf4j
@Service
public class SysSensitiveWordServiceImpl extends ServiceImpl<SysSensitiveWordMapper, SysSensitiveWord> implements SysSensitiveWordService {

    @Resource
    private SysSensitiveWordConvert sysSensitiveWordConvert;

    @Resource
    private SensitiveWordManager sensitiveWordManager;

    @Override
    public SysSensitiveWordVO getSensitiveWordById(Long id) {
        Assert.notNull(id, "敏感词ID不能为空");

        SysSensitiveWord sysSensitiveWord = this.getById(id);
        if (sysSensitiveWord == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_WORD_NOT_EXIST);
        }
        return sysSensitiveWordConvert.entityToVo(sysSensitiveWord);
    }

    @Override
    public IPage<SysSensitiveWordVO> pageAdminSensitiveWords(SysSensitiveWordQueryDTO queryDTO) {
        Assert.notNull(queryDTO, "分页查询参数不能为空");

        Page<SysSensitiveWord> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<SysSensitiveWord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(queryDTO.getWord()), SysSensitiveWord::getWord, queryDTO.getWord())
                .orderByDesc(SysSensitiveWord::getCreateTime);
        IPage<SysSensitiveWord> sensitiveWordPage = this.page(page, queryWrapper);
        return sensitiveWordPage.convert(sysSensitiveWord -> sysSensitiveWordConvert.entityToVo(sysSensitiveWord));
    }

    @Override
    public void addSensitiveWord(SysSensitiveWordAddDTO addDTO) {
        Assert.notNull(addDTO, "新增敏感词参数不能为空");

        SysSensitiveWord sysSensitiveWord = sysSensitiveWordConvert.addDtoToEntity(addDTO);
        try {
            if (this.save(sysSensitiveWord)) {
                sensitiveWordManager.refresh();
            }
        } catch (DuplicateKeyException e) {
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_WORD_EXIST);
        }
    }

    @Override
    public void updateSensitiveWord(SysSensitiveWordUpdateDTO updateDTO) {
        Assert.notNull(updateDTO, "更新敏感词参数不能为空");
        Assert.notNull(updateDTO.getId(), "敏感词ID不能为空");

        // 1. 必须先查出旧的敏感词内容，以便后面从内存中精准移除
        SysSensitiveWord oldWordData = this.getById(updateDTO.getId());
        if (oldWordData == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_WORD_NOT_EXIST);
        }

        LambdaQueryWrapper<SysSensitiveWord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysSensitiveWord::getWord, updateDTO.getWord())
                .ne(SysSensitiveWord::getId, updateDTO.getId());
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_WORD_EXIST);
        }

        LambdaUpdateWrapper<SysSensitiveWord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SysSensitiveWord::getWord, updateDTO.getWord())
                .eq(SysSensitiveWord::getId, updateDTO.getId());

        if (this.update(null, updateWrapper)) {
            sensitiveWordManager.refresh();
        }
    }

    @Override
    public void deleteSensitiveWordById(Long id) {
        Assert.notNull(id, "敏感词ID不能为空");

        // 1. 先查出这个词的具体内容
        SysSensitiveWord wordData = this.getById(id);

        // 2. 数据库删除成功后，将该词从内存中剔除
        if (wordData != null && this.removeById(id)) {
            sensitiveWordManager.refresh();
        }
    }

    @Override
    public void batchDeleteSensitiveWords(List<Long> ids) {
        Assert.notEmpty(ids, "敏感词ID列表不能为空");

        if (CollUtil.isEmpty(ids)) {
            return;
        }

        // 1. 先把准备删除的记录批量查出来，提取具体词汇内容
        List<SysSensitiveWord> wordList = this.listByIds(ids);

        // 2. 数据库批量删除成功后，循环从内存中剔除这些词汇
        if (CollUtil.isNotEmpty(wordList) && this.removeByIds(ids)) {
            sensitiveWordManager.refresh();
        }
    }
}