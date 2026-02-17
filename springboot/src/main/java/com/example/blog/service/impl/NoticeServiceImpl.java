package com.example.blog.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.convert.NoticeConvert;
import com.example.blog.dto.notice.NoticeAddDTO;
import com.example.blog.dto.notice.NoticeQueryDTO;
import com.example.blog.dto.notice.NoticeUpdateDTO;
import com.example.blog.entity.Notice;
import com.example.blog.exception.CustomerException;
import com.example.blog.mapper.NoticeMapper;
import com.example.blog.service.NoticeService;
import com.example.blog.utils.RedisUtil;
import com.example.blog.vo.notice.AdminNoticeVO;
import com.example.blog.vo.notice.NoticeVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (queryDTO == null) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_PARAM_ERROR);
        }
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
        if (addDTO == null) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_PARAM_ERROR);
        }

        Notice notice = noticeConvert.addDtoToEntity(addDTO);
        this.save(notice);

        // 只要有数据变动，清理缓存
        redisUtil.delete(RedisConstants.REDIS_NOTICE_LIST_KEY);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNotice(NoticeUpdateDTO updateDTO) {
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
        if (id == null) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_PARAM_ERROR);
        }

        boolean success = this.removeById(id);

        // 只要有数据变动，清理缓存
        if (success) {
            redisUtil.delete(RedisConstants.REDIS_NOTICE_LIST_KEY);
        }
    }

    @Override
    public void batchDeleteNotices(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_PARAM_ERROR);
        }
        this.removeByIds(ids);
        // 只要有数据变动，清理缓存
        redisUtil.delete(RedisConstants.REDIS_NOTICE_LIST_KEY);
    }

}
