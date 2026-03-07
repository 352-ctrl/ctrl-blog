package com.example.blog.modules.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.modules.article.model.dto.ArticleCategoryCountDTO;
import com.example.blog.modules.article.model.entity.Article;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章数据访问层
 * 继承MyBatis-Plus基础Mapper，提供基本CRUD操作
 * 对应实体类：Article
 * 对应数据库表：blog_article
 *
 * @see Article
 * @see BaseMapper
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 1. 【查询】找出回收站中过期的文章 ID
     * 自定义 SQL 不会受 @TableLogic 影响，可以查出 is_deleted=1 的数据
     */
    @Select("SELECT id FROM blog_article WHERE is_deleted = 1 AND delete_time < #{limitTime}")
    List<Integer> selectExpiredArticleIds(@Param("limitTime") LocalDateTime limitTime);

    /**
     * 2. 【物理删除】批量物理删除文章
     * 既然已经拿到了 ID List，直接用 ID 删效率最高
     */
    @Delete("<script>" +
            "DELETE FROM blog_article WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int physicalDeleteBatchIds(@Param("ids") List<Integer> ids);

    /**
     * 按分类统计已发布的文章数量
     * 直接映射到 DTO，无需中间 Map 转换
     */
    @Select("SELECT category_id, COUNT(*) as count " +
            "FROM blog_article " +
            "WHERE status = #{status} " +
            "GROUP BY category_id")
    List<ArticleCategoryCountDTO> selectCountByCategoryId(@Param("status") Integer status);

    /**
     * 文章点赞数 +1
     */
    @Update("UPDATE blog_article SET like_count = like_count + 1 WHERE id = #{articleId}")
    int incrLikeCount(@Param("articleId") Long articleId);

    /**
     * 文章点赞数 -1 (防止减为负数)
     */
    @Update("UPDATE blog_article SET like_count = like_count - 1 WHERE id = #{articleId} AND like_count > 0")
    int decrLikeCount(@Param("articleId") Long articleId);

    /**
     * 文章收藏数 +1
     * 原子更新，保证并发安全
     */
    @Update("UPDATE blog_article SET favorite_count = favorite_count + 1 WHERE id = #{articleId}")
    int incrFavoriteCount(@Param("articleId") Long articleId);

    /**
     * 文章收藏数 -1
     * 原子更新，防止减为负数
     */
    @Update("UPDATE blog_article SET favorite_count = favorite_count - 1 WHERE id = #{articleId} AND favorite_count > 0")
    int decrFavoriteCount(@Param("articleId") Long articleId);

}