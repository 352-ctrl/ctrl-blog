package com.example.blog.modules.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.modules.operation.model.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论点赞数据访问层
 * 处理用户对评论的点赞操作（点赞/取消点赞）
 * 对应实体类：CommentLike
 * 对应数据库表：blog_comment_like
 *
 * @see CommentLike
 * @see BaseMapper
 */
@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {

}