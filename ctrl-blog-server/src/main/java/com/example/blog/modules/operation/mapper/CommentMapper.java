package com.example.blog.modules.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.modules.operation.model.entity.Comment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章分类数据访问层
 * 继承MyBatis-Plus基础Mapper，提供基本CRUD操作
 * 对应实体类：Comment
 * 对应数据库表：blog_comment
 *
 * @see Comment
 * @see BaseMapper
 */
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 1. 清理单独被回收的评论
     * 场景：用户只删除了评论，没删文章。这些评论在回收站待满30天后需要清理。
     */
    @Delete("DELETE FROM blog_comment WHERE is_deleted = 1 AND delete_time < #{limitTime}")
    int physicalDeleteExpiredTrash(@Param("limitTime") LocalDateTime limitTime);

    /**
     * 2. 根据文章ID物理删除评论 (用于文章彻底删除时的级联清理)
     * 场景：文章被彻底物理删除了，它下面的评论不管有没有过期，都得陪葬。
     */
    @Delete("<script>" +
            "DELETE FROM blog_comment WHERE article_id IN " +
            "<foreach collection='articleIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int physicalDeleteByArticleIds(@Param("articleIds") List<Integer> articleIds);

    /**
     * 评论点赞数 +1
     */
    @Update("UPDATE blog_comment SET like_count = like_count + 1 WHERE id = #{commentId}")
    int incrLikeCount(@Param("commentId") Long commentId);

    /**
     * 评论点赞数 -1 (防止减为负数)
     */
    @Update("UPDATE blog_comment SET like_count = like_count - 1 WHERE id = #{commentId} AND like_count > 0")
    int decrLikeCount(@Param("commentId") Long commentId);

}