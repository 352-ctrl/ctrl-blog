package com.example.blog.modules.article.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.base.BaseLogicEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 文章实体类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("blog_article")
public class Article extends BaseLogicEntity {

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章封面
     */
    private String cover;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 摘要来源 0:人工 1:AI
     */
    private Integer isAiSummary;

    /**
     * 文章内容（Markdown格式）
     */
    private String content;

    /**
     * 文章内容（HTML格式）
     */
    private String contentHtml;

    /**
     * 浏览量
     */
    private Long viewCount;

    /**
     * 点赞数量
     */
    private Long likeCount;

    /**
     * 收藏数量
     */
    private Long favoriteCount;

    /**
     * 是否置顶，0：否，1：是
     */
    private BizStatus.Common isTop;

    /**
     * 是否首页轮播，0：否，1：是
     */
    private BizStatus.Common isCarousel;

    /**
     * 文章状态，1：发布，0：草稿
     */
    private BizStatus.Article status;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 用户ID
     */
    private Long userId;

}