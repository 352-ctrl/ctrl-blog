package com.example.blog.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.constants.RedisConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.convert.TagConvert;
import com.example.blog.dto.tag.TagAddDTO;
import com.example.blog.dto.tag.TagQueryDTO;
import com.example.blog.dto.tag.TagUpdateDTO;
import com.example.blog.entity.Article;
import com.example.blog.entity.ArticleTag;
import com.example.blog.entity.Tag;
import com.example.blog.exception.CustomerException;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.TagMapper;
import com.example.blog.service.ArticleTagService;
import com.example.blog.service.TagService;
import com.example.blog.utils.RedisUtil;
import com.example.blog.vo.TagVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 文章标签业务服务实现类
 * 实现标签相关的具体业务逻辑
 */
@Slf4j
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Resource
    private TagConvert tagConvert;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private ArticleTagService articleTagService;

    @Override
    public TagVO getTagById(Long id) {
        Assert.notNull(id, "标签ID不能为空");

        Tag tag = this.getById(id);
        if (tag == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_TAG_NOT_EXIST);
        }
        return tagConvert.entityToVo(tag);
    }

    /**
     * 获取前台展示用的标签列表 (前台高频接口，加入缓存)
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<TagVO> listPortalTags() {
        // 尝试从 Redis 获取
        try {
            List<TagVO> cachedList = (List<TagVO>) redisUtil.get(RedisConstants.REDIS_TAG_LIST_KEY);
            if (cachedList != null) {
                return cachedList;
            }
        } catch (Exception e) {
            log.error("Redis获取标签列表异常", e);
            redisUtil.delete(RedisConstants.REDIS_TAG_LIST_KEY);
        }

        List<Object> articleIds = articleMapper.selectObjs(
                new LambdaQueryWrapper<Article>()
                        .select(Article::getId)
                        .eq(Article::getStatus, BizStatus.Article.PUBLISHED)
        );

        if (CollUtil.isEmpty(articleIds)) {
            return Collections.emptyList();
        }

        List<ArticleTag> articleTags = articleTagService.list(
                new LambdaQueryWrapper<ArticleTag>()
                        .select(ArticleTag::getTagId)
                        .in(ArticleTag::getArticleId, articleIds)
                        .groupBy(ArticleTag::getTagId) // 数据库层去重
        );
        if (CollUtil.isEmpty(articleTags)) {
            return new ArrayList<>();
        }

        Set<Long> validTagIds = articleTags.stream()
                .map(ArticleTag::getTagId)
                .collect(Collectors.toSet());

        // 查询数据库
        List<Tag> tagList = this.listByIds(validTagIds);

        List<TagVO> activeTagVOS = tagConvert.entitiesToVos(tagList);

        // 写入 Redis (设置过期时间 1 天)
        redisUtil.set(RedisConstants.REDIS_TAG_LIST_KEY, activeTagVOS, RedisConstants.EXPIRE_METADATA, TimeUnit.DAYS);

        return activeTagVOS;
    }

    /**
     * 获取所有标签
     */
    @Override
    public List<TagVO> listAdminTags() {
        // 查询数据库
        List<Tag> tagList = this.list();
        return tagConvert.entitiesToVos(tagList);
    }

    @Override
    public IPage<TagVO> pageAdminTags(TagQueryDTO queryDTO) {
        Assert.notNull(queryDTO, "分页查询参数不能为空");

        Page<Tag> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(queryDTO.getName()), Tag::getName, queryDTO.getName())
                .orderByDesc(Tag::getCreateTime);
        IPage<Tag> tagPage = this.page(page, queryWrapper);
        return tagPage.convert(tag -> tagConvert.entityToVo(tag));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTag(TagAddDTO addDTO) {
        Assert.notNull(addDTO, "新增标签参数不能为空");

        Tag tag = tagConvert.addDtoToEntity(addDTO);
        try {
            this.save(tag);
        } catch (DuplicateKeyException e) {
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_TAG_EXIST);
        }

        // 只要有数据变动，清理缓存
        redisUtil.delete(RedisConstants.REDIS_TAG_LIST_KEY);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTag(TagUpdateDTO updateDTO) {
        Assert.notNull(updateDTO, "更新标签参数不能为空");
        Assert.notNull(updateDTO.getId(), "标签ID不能为空");

        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Tag::getName, updateDTO.getName())
                .ne(Tag::getId, updateDTO.getId());
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_TAG_EXIST);
        }

        LambdaUpdateWrapper<Tag> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Tag::getName, updateDTO.getName())
                .eq(Tag::getId, updateDTO.getId());

        boolean success = this.update(null, updateWrapper);

        if (!success) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_TAG_NOT_EXIST);
        }

        // 只要有数据变动，清理缓存
        redisUtil.delete(RedisConstants.REDIS_TAG_LIST_KEY);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTagById(Long id) {
        Assert.notNull(id, "标签ID不能为空");

        boolean success = this.removeById(id);

        // 只要有数据变动，清理缓存
        if (success) {
            redisUtil.delete(RedisConstants.REDIS_TAG_LIST_KEY);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteTags(List<Long> ids) {
        Assert.notEmpty(ids, "标签ID列表不能为空");

        this.removeBatchByIds(ids);

        // 只要有数据变动，清理缓存
        redisUtil.delete(RedisConstants.REDIS_TAG_LIST_KEY);
    }
}