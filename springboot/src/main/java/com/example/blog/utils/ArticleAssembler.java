package com.example.blog.utils;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.entity.Article;
import com.example.blog.entity.ArticleTag;
import com.example.blog.entity.Category;
import com.example.blog.entity.Tag;
import com.example.blog.entity.User;
import com.example.blog.mapper.ArticleTagMapper;
import com.example.blog.mapper.CategoryMapper;
import com.example.blog.mapper.TagMapper;
import com.example.blog.mapper.UserMapper;
import com.example.blog.vo.TagVO;
import com.example.blog.vo.user.UserVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 文章附加信息查询工具类
 * 功能：批量查询文章关联的作者/分类/标签信息，返回多个语义化Map，避免循环SQL
 * 核心规范：1. 批量查询优先；2. 实体纯净不修改；3. 空值兜底避免NPE
 */
@Component
public class ArticleAssembler {

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
     *
     * @param articles 文章实体列表，为空时返回空Map兜底，非空时批量查询关联信息
     * @return Map<String, Object> 结构化附加信息Map，key及对应值说明：
     * <ul>
     * <li>userIdToUserMap：Map<Long, UserVO> - 用户ID -> UserVO(包含昵称/头像)</li>
     * <li>categoryIdToNameMap：Map<Long, String> - 分类ID -> 分类名称</li>
     * <li><strong>articleIdToTagsMap</strong> : {@code Map<Long, List<TagVO>>} - 文章ID -> 标签VO列表(包含ID和Name)</li>
     * </ul>
     */
    public Map<String, Object> batchQueryArticleExtraMaps(List<Article> articles) {
        // 复用 querySearchIndexExtraMaps 获取分类和标签信息
        Map<String, Object> baseMap = queryBaseArticleExtraMaps(articles);

        Map<String, Object> resultMap = new HashMap<>(baseMap);

        // 参数校验与防御性处理
        if (CollUtil.isEmpty(articles)) {
            resultMap.put("userIdToUserMap", new HashMap<Long, UserVO>());
            return resultMap;
        }

        // 提取 ID 集合 (使用 Set 去重)
        Set<Long> userIds = articles.stream()
                .map(Article::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 批量查询用户 → UserVO（包含nickname/avatar）
        Map<Long, UserVO> userIdToUserMap = new HashMap<>();
        if (CollUtil.isNotEmpty(userIds)) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .in(User::getId, userIds)
                    .select(User::getId, User::getNickname, User::getAvatar));
            userIdToUserMap = users.stream()
                    .collect(Collectors.toMap(
                            User::getId,
                            // Value Mapper: 直接 new VO
                            user -> UserVO.builder()
                                    .id(user.getId())
                                    .nickname(user.getNickname())
                                    .avatar(user.getAvatar())
                                    .build(),
                            (k1, k2) -> k1
                    ));
        }

        resultMap.put("userIdToUserMap", userIdToUserMap);

        return resultMap;
    }

    /**
     * 批量组装文章的基础关联信息（分类、标签）。
     * <p>
     * 针对传入的文章列表，批量查询数据库以避免 N+1 问题，并以 Map 形式返回聚合数据。
     *
     * @param articles 文章实体列表。若为空或 null，直接返回空 Map 以防止空指针。
     * @return {@code Map<String, Object>} 包含所有关联信息的复合 Map，Key 定义如下：
     * <ul>
     * <li><strong>categoryIdToNameMap</strong> : {@code Map<Long, String>} - 分类ID -> 分类名称</li>
     * <li><strong>articleIdToTagsMap</strong> : {@code Map<Long, List<TagVO>>} - 文章ID -> 标签VO列表(包含ID和Name)</li>
     * </ul>
     */
    public Map<String, Object> queryBaseArticleExtraMaps(List<Article> articles) {
        // 最终返回的Map（存放多个子Map）
        Map<String, Object> resultMap = new HashMap<>();

        // 参数校验与防御性处理
        if (CollUtil.isEmpty(articles)) {
            resultMap.put("categoryIdToNameMap", new HashMap<Integer, String>());
            resultMap.put("articleIdToTagsMap", new HashMap<Integer, List<TagVO>>());
            return resultMap;
        }

        // 提取 ID 集合 (使用 Set 去重)
        Set<Long> categoryIds = articles.stream()
                .map(Article::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> articleIds = articles.stream()
                .map(Article::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 处理分类：ID -> Name 映射
        Map<Long, String> categoryIdToNameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(categoryIds)) {
            List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                    .in(Category::getId, categoryIds)
                    .select(Category::getId, Category::getName));
            categoryIdToNameMap = categories.stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName));
        }

        // 处理标签：文章ID -> TagVO列表 映射
        Map<Long, List<TagVO>> articleIdToTagsMap = new HashMap<>();
        if (CollUtil.isNotEmpty(articleIds)) {
            // 查询中间表 (ArticleTag)
            List<ArticleTag> articleTags = articleTagMapper.selectList(new LambdaQueryWrapper<ArticleTag>()
                    .in(ArticleTag::getArticleId, articleIds)
                    .select(ArticleTag::getArticleId, ArticleTag::getTagId));
            if (CollUtil.isNotEmpty(articleTags)) {
                // 提取涉及的所有标签 ID
                Set<Long> tagIds = articleTags.stream()
                        .map(ArticleTag::getTagId)
                        .collect(Collectors.toSet());

                // 查询标签表 (Tag) 并转为 Map<ID, Name>
                Map<Long, String> tagIdNameMap = new HashMap<>();
                if (CollUtil.isNotEmpty(tagIds)) {
                    List<Tag> tags = tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                            .in(Tag::getId, tagIds)
                            .select(Tag::getId, Tag::getName));

                    tagIdNameMap = tags.stream()
                            .collect(Collectors.toMap(Tag::getId, Tag::getName));
                }

                // 内存组装 (Join Logic)
                final Map<Long, String> finalTagMap = tagIdNameMap;
                articleIdToTagsMap = articleTags.stream()
                        // 过滤脏数据 (关联表中存在，但 Tag 表中已删除的 ID)
                        .filter(at -> finalTagMap.containsKey(at.getTagId()))
                        .collect(Collectors.groupingBy(
                                ArticleTag::getArticleId,
                                Collectors.mapping(at -> {
                                    // 直接构建 VO 对象
                                    return TagVO.builder()
                                            .id(at.getTagId())
                                            .name(finalTagMap.get(at.getTagId()))
                                            .build();
                                }, Collectors.toList())
                        ));
            }
        }

        resultMap.put("categoryIdToNameMap", categoryIdToNameMap);
        resultMap.put("articleIdToTagsMap", articleIdToTagsMap);

        return resultMap;
    }

    /**
     * 查询单条文章的关联附加信息（作者、分类、标签）
     * 这是一个便利方法，内部调用批量查询逻辑，保持代码复用
     *
     * @param article 单条文章实体
     * @return Map<String, Object> 结构化附加信息Map
     */
    public Map<String, Object> queryArticleExtraMaps(Article article) {
        if (article == null) {
            return new HashMap<>();
        }
        // 复用核心逻辑：把单条包装成 List 调用
        return batchQueryArticleExtraMaps(Collections.singletonList(article));
    }
}
