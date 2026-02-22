package com.example.blog.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.convert.SysSensitiveWordConvert;
import com.example.blog.dto.word.SysSensitiveWordAddDTO;
import com.example.blog.dto.word.SysSensitiveWordQueryDTO;
import com.example.blog.dto.word.SysSensitiveWordUpdateDTO;
import com.example.blog.entity.SysSensitiveWord;
import com.example.blog.exception.CustomerException;
import com.example.blog.mapper.SysSensitiveWordMapper;
import com.example.blog.service.SysSensitiveWordService;
import com.example.blog.utils.SensitiveWordManager;
import com.example.blog.vo.SysSensitiveWordVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

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
                // 变更后同步刷新内存词库
                sensitiveWordManager.initWordTree();
            }
        } catch (DuplicateKeyException e) {
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_WORD_EXIST);
        }
    }

    @Override
    public void updateSensitiveWord(SysSensitiveWordUpdateDTO updateDTO) {
        Assert.notNull(updateDTO, "更新敏感词参数不能为空");
        Assert.notNull(updateDTO.getId(), "敏感词ID不能为空");

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
            // 更新成功后刷新
            sensitiveWordManager.initWordTree();
        } else {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_WORD_NOT_EXIST);
        }
    }

    @Override
    public void deleteSensitiveWordById(Long id) {
        Assert.notNull(id, "敏感词ID不能为空");

        if (this.removeById(id)) {
            // 删除后刷新
            sensitiveWordManager.initWordTree();
        }
    }

    @Override
    public void batchDeleteSensitiveWords(List<Long> ids) {
        Assert.notEmpty(ids, "敏感词ID列表不能为空");

        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        if (this.removeByIds(ids)) {
            // 批量删除后刷新
            sensitiveWordManager.initWordTree();
        }
    }
}