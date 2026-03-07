package com.example.blog.modules.article.builder;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.modules.article.model.bo.ArticleExtraContext;
import com.example.blog.modules.article.model.entity.Article;
import com.example.blog.modules.article.model.entity.ArticleTag;
import com.example.blog.modules.article.model.entity.Category;
import com.example.blog.modules.article.model.entity.Tag;
import com.example.blog.modules.user.model.entity.User;
import com.example.blog.modules.article.mapper.ArticleTagMapper;
import com.example.blog.modules.article.mapper.CategoryMapper;
import com.example.blog.modules.article.mapper.TagMapper;
import com.example.blog.modules.user.mapper.UserMapper;
import com.example.blog.modules.article.model.vo.TagVO;
import com.example.blog.modules.user.model.vo.UserVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 文章附加信息查询工具类
 * 功能：批量查询文章关联的作者/分类/标签信息
 */
@Component
public class ArticleContextBuilder {

    @Resource
    private UserMapper userMapper;
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private ArticleTagMapper articleTagMapper;
    @Resource
    private TagMapper tagMapper;

    /**
     * 批量获取文章关联的附加信息（作者、分类、标签）
     */
    public ArticleExtraContext batchQueryArticleExtraContext(List<Article> articles) {
        if (CollUtil.isEmpty(articles)) {
            return new ArticleExtraContext();
        }

        // 1. 获取基础上下文（分类、标签）
        ArticleExtraContext context = queryBaseArticleExtraContext(articles);

        // 2. 提取并查询用户 ID
        Set<Long> userIds = articles.stream()
                .map(Article::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (CollUtil.isNotEmpty(userIds)) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .in(User::getId, userIds)
                    .select(User::getId, User::getNickname, User::getAvatar));

            Map<Long, UserVO> userMap = users.stream()
                    .collect(Collectors.toMap(
                            User::getId,
                            user -> UserVO.builder()
                                    .id(user.getId())
                                    .nickname(user.getNickname())
                                    .avatar(user.getAvatar())
                                    .build(),
                            (k1, k2) -> k1
                    ));
            context.setUserMap(userMap);
        }

        return context;
    }

    /**
     * 批量组装文章的基础关联信息（分类、标签）
     */
    public ArticleExtraContext queryBaseArticleExtraContext(List<Article> articles) {
        ArticleExtraContext context = new ArticleExtraContext();

        if (CollUtil.isEmpty(articles)) {
            return context;
        }

        Set<Long> categoryIds = articles.stream()
                .map(Article::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> articleIds = articles.stream()
                .map(Article::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 处理分类
        if (CollUtil.isNotEmpty(categoryIds)) {
            List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                    .in(Category::getId, categoryIds)
                    .select(Category::getId, Category::getName));
            Map<Long, String> categoryMap = categories.stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName));
            context.setCategoryMap(categoryMap);
        }

        // 处理标签
        if (CollUtil.isNotEmpty(articleIds)) {
            List<ArticleTag> articleTags = articleTagMapper.selectList(new LambdaQueryWrapper<ArticleTag>()
                    .in(ArticleTag::getArticleId, articleIds)
                    .select(ArticleTag::getArticleId, ArticleTag::getTagId));

            if (CollUtil.isNotEmpty(articleTags)) {
                Set<Long> tagIds = articleTags.stream()
                        .map(ArticleTag::getTagId)
                        .collect(Collectors.toSet());

                if (CollUtil.isNotEmpty(tagIds)) {
                    List<Tag> tags = tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                            .in(Tag::getId, tagIds)
                            .select(Tag::getId, Tag::getName));
                    Map<Long, String> tagIdNameMap = tags.stream()
                            .collect(Collectors.toMap(Tag::getId, Tag::getName));

                    Map<Long, List<TagVO>> tagMap = articleTags.stream()
                            .filter(at -> tagIdNameMap.containsKey(at.getTagId()))
                            .collect(Collectors.groupingBy(
                                    ArticleTag::getArticleId,
                                    Collectors.mapping(at -> TagVO.builder()
                                            .id(at.getTagId())
                                            .name(tagIdNameMap.get(at.getTagId()))
                                            .build(), Collectors.toList())
                            ));
                    context.setTagMap(tagMap);
                }
            }
        }

        return context;
    }

    /**
     * 查询单条文章的关联附加信息
     */
    public ArticleExtraContext queryArticleExtraContext(Article article) {
        if (article == null) {
            return new ArticleExtraContext();
        }
        return batchQueryArticleExtraContext(Collections.singletonList(article));
    }
}