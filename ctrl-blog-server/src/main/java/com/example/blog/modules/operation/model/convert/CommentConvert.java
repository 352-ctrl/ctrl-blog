package com.example.blog.modules.operation.model.convert;

import com.example.blog.modules.operation.model.bo.CommentExtraContext;
import com.example.blog.common.base.BaseConvert;
import com.example.blog.modules.operation.model.dto.CommentAddDTO;
import com.example.blog.modules.operation.model.entity.Comment;
import com.example.blog.modules.operation.model.vo.AdminCommentVO;
import com.example.blog.modules.operation.model.vo.CommentVO;
import org.mapstruct.*;

import java.util.List;

/**
 * 评论专属转换接口（继承通用接口）
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = BaseConvert.class
)
public interface CommentConvert extends BaseConvert<Comment, CommentAddDTO, Void, AdminCommentVO> {

    @Override
    Comment addDtoToEntity(CommentAddDTO addDTO);

    @Override
    @Named("entityToAdminVoBasic")
    AdminCommentVO entityToVo(Comment comment);

    /**
     * 单个评论实体转换为后台管理专用-评论VO
     */
    @Mappings({
            @Mapping(target = "userNickname", expression = "java(context.getUserNickname(comment.getUserId()))"),
            @Mapping(target = "userAvatar", expression = "java(context.getUserAvatar(comment.getUserId()))"),
            @Mapping(target = "articleTitle", expression = "java(context.getArticleTitle(comment.getArticleId()))"),
            @Mapping(target = "replyUserNickname", expression = "java(context.getReplyUserNickname(comment.getReplyUserId()))"),
            @Mapping(target = "replyContent", expression = "java(context.getReplyContent(getTargetId(comment)))")
    })
    AdminCommentVO entityToAdminVo(Comment comment, @Context CommentExtraContext context);

    List<AdminCommentVO> entitiesToAdminVos(List<Comment> comments, @Context CommentExtraContext context);

    /**
     * 单个评论实体转换为评论列表VO
     */
    @Mappings({
            @Mapping(target = "userNickname", expression = "java(context.getUserNickname(comment.getUserId()))"),
            @Mapping(target = "userAvatar", expression = "java(context.getUserAvatar(comment.getUserId()))"),
            @Mapping(target = "replyUserNickname", expression = "java(context.getReplyUserNickname(comment.getReplyUserId()))")
    })
    CommentVO entityToFrontVo(Comment comment, @Context CommentExtraContext context);

    List<CommentVO> entitiesToFrontVos(List<Comment> comments, @Context CommentExtraContext context);

    default Long getTargetId(Comment comment) {
        if (comment.getReplyCommentId() != null && comment.getReplyCommentId() > 0) {
            return comment.getReplyCommentId();
        } else if (comment.getParentId() != null && comment.getParentId() > 0) {
            return comment.getParentId();
        }
        return null;
    }
}