package com.example.blog.modules.article.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.modules.article.model.entity.ArticleTag;
import com.example.blog.modules.article.mapper.ArticleTagMapper;
import com.example.blog.modules.article.service.ArticleTagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文章标签业务服务实现类
 * 实现标签相关的具体业务逻辑
 */
@Service
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagMapper, ArticleTag> implements ArticleTagService {

    @Override
    public List<Long> listArticleIdsByTagIds(List<Long> tagIds) {
        if (CollUtil.isEmpty(tagIds)) {
            return Collections.emptyList();
        }

        // 1. 将标签对应的所有关联记录都查出来
        List<ArticleTag> list = this.list(new LambdaQueryWrapper<ArticleTag>()
                .in(ArticleTag::getTagId, tagIds));

        // 2. 在内存中进行分组和筛选 (Java Stream 流)
        return list.stream()
                // 按照文章ID分组，看每篇文章占了几个标签
                .collect(Collectors.groupingBy(ArticleTag::getArticleId))
                .entrySet().stream()
                // 筛选：只有当这篇文章拥有的标签数量 == 目标标签数量时，才符合条件
                .filter(entry -> entry.getValue().size() == tagIds.size())
                // 提取文章ID
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 更新文章标签关联
     * 逻辑：查出旧的 -> 计算差集 -> 删除多余的 -> 添加新增的
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 涉及写操作，建议加事务
    public void updateArticleTags(Long articleId, List<Long> tagIds) {
        Assert.notNull(articleId, "文章ID不能为空");

        // 0. 判空处理：如果传入 null，视为空列表（即删除所有标签）
        if (tagIds == null) {
            tagIds = Collections.emptyList();
        }

        // 1. 查出数据库里现有的标签ID列表
        // 注意：这里是在 Service 内部，直接用 this 调用 IService 的方法
        List<Long> oldTagIds = this.list(new LambdaQueryWrapper<ArticleTag>()
                        .eq(ArticleTag::getArticleId, articleId))
                .stream()
                .map(ArticleTag::getTagId)
                .toList();

        // 2. 计算需要删除的 (Old - New)
        // 必须用 final 变量或者新的 effectively final 变量给 stream 使用
        List<Long> finalTagIds = tagIds;
        List<Long> deleteIds = oldTagIds.stream()
                .filter(id -> !finalTagIds.contains(id))
                .collect(Collectors.toList());

        // 3. 计算需要新增的 (New - Old)
        List<Long> addIds = tagIds.stream()
                .filter(id -> !oldTagIds.contains(id))
                .collect(Collectors.toList());

        // 4. 执行删除操作
        if (CollUtil.isNotEmpty(deleteIds)) {
            this.remove(new LambdaQueryWrapper<ArticleTag>()
                    .eq(ArticleTag::getArticleId, articleId)
                    .in(ArticleTag::getTagId, deleteIds));
        }

        // 5. 执行新增操作
        if (CollUtil.isNotEmpty(addIds)) {
            List<ArticleTag> articleTagList = addIds.stream()
                    .map(tagId -> {
                        ArticleTag articleTag = new ArticleTag();
                        articleTag.setArticleId(articleId);
                        articleTag.setTagId(tagId);
                        return articleTag;
                    })
                    .collect(Collectors.toList());
            this.saveBatch(articleTagList);
        }
    }

}