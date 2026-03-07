package com.example.blog.modules.article.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.blog.common.base.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 文章标签关联实体类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("blog_article_tag")
public class ArticleTag extends BaseEntity {

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 标签ID
     */
    private Long tagId;

}