package com.example.blog.service.impl;

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
import com.example.blog.convert.CategoryConvert;
import com.example.blog.dto.category.CategoryAddDTO;
import com.example.blog.dto.category.CategoryQueryDTO;
import com.example.blog.dto.category.CategoryUpdateDTO;
import com.example.blog.entity.Article;
import com.example.blog.entity.Category;
import com.example.blog.exception.CustomerException;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.CategoryMapper;
import com.example.blog.service.CategoryService;
import com.example.blog.utils.RedisUtil;
import com.example.blog.vo.CategoryVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 文章分类业务服务实现类
 * 实现分类相关的具体业务逻辑
 */
@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Resource
    private CategoryConvert categoryConvert;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public CategoryVO getCategoryById(Long id) {
        Assert.notNull(id, "分类ID不能为空");

        Category category = this.getById(id);
        if (category == null) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_TAG_NOT_EXIST);
        }
        return categoryConvert.entityToVo(category);
    }

    /**
     * 获取前台展示用的分类列表 (带缓存)
     * 逻辑：查询分类 + 过滤掉没有文章的分类
     */
    @SuppressWarnings("unchecked")
    public List<CategoryVO> listPortalCategories() {
        // 1. 尝试从 Redis 获取
        try {
            List<CategoryVO> cachedList = (List<CategoryVO>) redisUtil.get(RedisConstants.REDIS_CATEGORY_LIST_KEY);
            if (cachedList != null) {
                return cachedList;
            }
        } catch (Exception e) {
            log.error("Redis获取分类列表异常", e);
            redisUtil.delete(RedisConstants.REDIS_CATEGORY_LIST_KEY);
        }

        // 2. 调用 ArticleService 获取所有有文章的分类ID集合
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .select(Article::getCategoryId)
                        .eq(Article::getStatus, BizStatus.Article.PUBLISHED)
        );

        if (CollectionUtils.isEmpty(articles)) {
            return new ArrayList<>();
        }

        Set<Long> existingCategoryIds = articles.stream()
                .map(Article::getCategoryId)
                .collect(Collectors.toSet());

        // 3. 调用 CategoryService 获取所有分类基础数据
        List<Category> categoryList = this.list();
        List<CategoryVO> categoryVOS = categoryConvert.entitiesToVos(categoryList);

        // 4. 在内存中进行聚合/过滤 (核心业务逻辑)
        List<CategoryVO> activeCategoryVOS = categoryVOS.stream()
                .filter(vo -> existingCategoryIds.contains(vo.getId()))
                .toList();

        // 5. 写入 Redis
        redisUtil.set(RedisConstants.REDIS_CATEGORY_LIST_KEY, activeCategoryVOS, RedisConstants.EXPIRE_METADATA, TimeUnit.DAYS);

        return activeCategoryVOS;
    }

    /**
     * 获取所有分类
     */
    @Override
    public List<CategoryVO> listAdminCategories() {
        // 查询数据库
        List<Category> categoryList = this.list();
        return categoryConvert.entitiesToVos(categoryList);
    }

    @Override
    public IPage<CategoryVO> pageAdminCategories(CategoryQueryDTO queryDTO) {
        Assert.notNull(queryDTO, "查询条件不能为空");

        Page<Category> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(queryDTO.getName()), Category::getName, queryDTO.getName())
                .orderByDesc(Category::getCreateTime);
        IPage<Category> categoryPage = this.page(page, queryWrapper);
        return categoryPage.convert(category -> categoryConvert.entityToVo(category));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCategory(CategoryAddDTO addDTO) {
        Assert.notNull(addDTO, "新增分类参数不能为空");

        Category category = categoryConvert.addDtoToEntity(addDTO);
        try {
            this.save(category);
        } catch (DuplicateKeyException e) {
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_CATEGORY_EXIST);
        }

        // 只要有数据变动，清理缓存
        redisUtil.delete(RedisConstants.REDIS_CATEGORY_LIST_KEY);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(CategoryUpdateDTO updateDTO) {
        Assert.notNull(updateDTO, "更新分类参数不能为空");
        Assert.notNull(updateDTO.getId(), "分类ID不能为空");

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, updateDTO.getName())
                .ne(Category::getId, updateDTO.getId());
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new CustomerException(ResultCode.CONFLICT, MessageConstants.MSG_CATEGORY_EXIST);
        }

        LambdaUpdateWrapper<Category> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Category::getName, updateDTO.getName())
                .eq(Category::getId, updateDTO.getId());

        boolean success = this.update(null, updateWrapper);

        if (!success) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_CATEGORY_NOT_EXIST);
        }

        // 只要有数据变动，清理缓存
        redisUtil.delete(RedisConstants.REDIS_CATEGORY_LIST_KEY);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategoryById(Long id) {
        Assert.notNull(id, "分类ID不能为空");

        boolean success = this.removeById(id);

        // 只要有数据变动，清理缓存
        if (success) {
            redisUtil.delete(RedisConstants.REDIS_CATEGORY_LIST_KEY);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteCategories(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            log.warn("批量删除分类失败：传入的 ID 列表为空");
            return;
        }

        this.removeByIds(ids);

        // 只要有数据变动，清理缓存
        redisUtil.delete(RedisConstants.REDIS_CATEGORY_LIST_KEY);
    }
}