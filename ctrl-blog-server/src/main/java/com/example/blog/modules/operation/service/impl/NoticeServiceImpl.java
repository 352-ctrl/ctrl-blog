package com.example.blog.modules.operation.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.modules.operation.model.convert.NoticeConvert;
import com.example.blog.modules.operation.model.dto.NoticeAddDTO;
import com.example.blog.modules.operation.model.dto.NoticeQueryDTO;
import com.example.blog.modules.operation.model.dto.NoticeUpdateDTO;
import com.example.blog.modules.operation.model.entity.Notice;
import com.example.blog.core.exception.CustomerException;
import com.example.blog.modules.operation.mapper.NoticeMapper;
import com.example.blog.modules.operation.service.NoticeService;
import com.example.blog.common.utils.RedisUtil;
import com.example.blog.modules.operation.model.vo.AdminNoticeVO;
import com.example.blog.modules.operation.model.vo.NoticeVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 系统公告业务服务实现类
 * 实现公告相关的具体业务逻辑
 */
@Slf4j
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Resource
    private NoticeConvert noticeConvert;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public AdminNoticeVO getNoticeById(Long id) {
        Assert.notNull(id, "公告ID不能为空");

        Notice notice = this.getById(id);
        if (notice == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_NOTICE_NOT_EXIST);
        }
        return noticeConvert.entityToAdminVo(notice);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<NoticeVO> listScrollNotices() {
        // 尝试从 Redis 获取
        try {
            List<NoticeVO> cachedList = (List<NoticeVO>) redisUtil.get(RedisConstants.REDIS_NOTICE_LIST_KEY);
            if (cachedList != null) {
                return cachedList;
            }
        } catch (Exception e) {
            log.error("Redis获取公告列表异常", e);
            redisUtil.delete(RedisConstants.REDIS_NOTICE_LIST_KEY);
        }

        // 查询数据库
        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notice::getStatus, BizStatus.Article.PUBLISHED)
                .orderByDesc(Notice::getIsTop, Notice::getCreateTime)
                .last("LIMIT 5");
        List<Notice> noticeList = this.list(queryWrapper);
        List<NoticeVO> noticeVOList = noticeConvert.entitiesToVos(noticeList);

        // 写入 Redis (设置过期时间 1 天)
        redisUtil.set(RedisConstants.REDIS_NOTICE_LIST_KEY, noticeVOList, RedisConstants.EXPIRE_NOTICE_LIST, TimeUnit.DAYS);
        return noticeVOList;
    }

    @Override
    public IPage<AdminNoticeVO> pageAdminNotices(NoticeQueryDTO queryDTO) {
        Assert.notNull(queryDTO, "分页查询参数不能为空");

        Page<Notice> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(queryDTO.getContent()), Notice::getContent, queryDTO.getContent())
                .eq(queryDTO.getStatus() != null, Notice::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getIsTop() != null, Notice::getIsTop, queryDTO.getIsTop())
                .orderByDesc(Notice::getIsTop, Notice::getCreateTime);
        IPage<Notice> noticePage = this.page(page, queryWrapper);
        // 转换为VO，并设置HTML内容
        return noticePage.convert(notice -> noticeConvert.entityToAdminVo(notice));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addNotice(NoticeAddDTO addDTO) {
        Assert.notNull(addDTO, "新增公告参数不能为空");

        Notice notice = noticeConvert.addDtoToEntity(addDTO);
        this.save(notice);

        // 只要有数据变动，清理缓存
        redisUtil.delete(RedisConstants.REDIS_NOTICE_LIST_KEY);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNotice(NoticeUpdateDTO updateDTO) {
        Assert.notNull(updateDTO, "更新公告参数不能为空");
        Assert.notNull(updateDTO.getId(), "公告ID不能为空");

        Notice notice = this.getById(updateDTO.getId());
        if (notice == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_NOTICE_NOT_EXIST);
        }

        noticeConvert.updateEntityFromDto(updateDTO, notice);
        this.updateById(notice);

        // 只要有数据变动，清理缓存
        redisUtil.delete(RedisConstants.REDIS_NOTICE_LIST_KEY);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNoticeById(Long id) {
        Assert.notNull(id, "公告ID不能为空");

        boolean success = this.removeById(id);

        // 只要有数据变动，清理缓存
        if (success) {
            redisUtil.delete(RedisConstants.REDIS_NOTICE_LIST_KEY);
        }
    }

    @Override
    public void batchDeleteNotices(List<Long> ids) {
        Assert.notEmpty(ids, "公告ID列表不能为空");

        this.removeByIds(ids);
        // 只要有数据变动，清理缓存
        redisUtil.delete(RedisConstants.REDIS_NOTICE_LIST_KEY);
    }

}