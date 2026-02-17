package com.example.blog.convert;

import com.example.blog.dto.comment.CommentAddDTO;
import com.example.blog.entity.Comment;
import com.example.blog.vo.UserVO;
import com.example.blog.vo.comment.AdminCommentVO;
import com.example.blog.vo.comment.CommentVO;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 评论专属转换接口（继承通用接口）
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = BaseConvert.class // 关联通用转换接口
)
public interface CommentConvert extends BaseConvert<Comment, CommentAddDTO, Void, AdminCommentVO> {

    /**
     * 新增DTO转换为评论实体
     *
     * @param addDTO 评论新增DTO，为null时返回null
     * @return 评论实体
     */
    @Override
    Comment addDtoToEntity(CommentAddDTO addDTO);

    @Override
    @Named("entityToAdminVoBasic") // 唯一名称：无参基础转换
    AdminCommentVO entityToVo(Comment comment);

    /**
     * 单个评论实体转换为后台管理专用-评论VO
     *
     * @param comment    评论实体，为null时返回null
     * @param extraMaps  上下文参数-评论附加信息Map（来源于CommentInfoFillUtil批量查询），核心key说明：
     *                   <ul>
     *                   <li>userIdToUserMap：Map<Long, UserVO> - 用户ID -> UserVO(包含昵称/头像)</li>
     *                   <li>articleIdToTitleMap: Map<Long, String> - 文章ID映射文章标题</li>
     *                   <li>replyUserIdToNicknameMap: Map<Long, String> - 被回复的用户ID映射被回复的用户昵称/li>
     *                   <li>replyCommentIdToReplyContent：Map<Long, String> - 被回复的评论ID映射被回复的评论内容摘要</li>
     *                   </ul>
     * @return 后台管理专用-评论VO（CommentAdminVO），实体为null时返回null
     */
    @Mappings({
            @Mapping(target = "userNickname", expression = "java(this.getUserNickname(comment, extraMaps))"),
            @Mapping(target = "userAvatar", expression = "java(this.getUserAvatar(comment, extraMaps))"),
            @Mapping(target = "articleTitle", expression = "java(this.getArticleTitle(comment, extraMaps))"),
            @Mapping(target = "replyUserNickname", expression = "java(this.getReplyUserNickname(comment, extraMaps))"),
            @Mapping(target = "replyContent", expression = "java(this.getReplyContent(comment, extraMaps))")
    })
    AdminCommentVO entityToAdminVo(Comment comment, @Context Map<String, Object> extraMaps);

    /**
     * 评论实体列表转换为后台管理专用-评论VO列表
     * <p>内部复用{@link #entityToAdminVo(Comment, Map)}方法实现单个实体的转换逻辑</p>
     *
     * @param comments   评论实体列表，为null/空列表时返回空列表（非null）
     * @param extraMaps  上下文参数-评论附加信息Map，同{@link #entityToAdminVo(Comment, Map)}的extraMaps参数说明
     * @return 后台管理专用-评论VO列表（CommentAdminVO），入参为null/空时返回空列表
     */
    List<AdminCommentVO> entitiesToAdminVos(List<Comment> comments, @Context Map<String, Object> extraMaps);

    /**
     * 单个评论实体转换为评论列表VO
     *
     * @param comment    评论实体，为null时返回null
     * @param extraMaps  上下文参数-评论附加信息Map（来源于CommentInfoFillUtil批量查询），核心key说明：
     *                   <ul>
     *                   <li>userIdToUserMap：Map<Long, UserVO> - 用户ID -> UserVO(包含昵称/头像)</li>
     *                   <li>replyUserIdToNicknameMap: Map<Long, String> - 被回复的用户ID映射被回复的用户昵称/li>
     *                   </ul>
     * @return 评论列表VO（CommentVO），实体为null时返回null
     */
    @Mappings({
            @Mapping(target = "userNickname", expression = "java(this.getUserNickname(comment, extraMaps))"),
            @Mapping(target = "userAvatar", expression = "java(this.getUserAvatar(comment, extraMaps))"),
            @Mapping(target = "replyUserNickname", expression = "java(this.getReplyUserNickname(comment, extraMaps))")
    })
    CommentVO entityToFrontVo(Comment comment, @Context Map<String, Object> extraMaps);

    /**
     * 评论实体列表转换为评论VO列表
     * <p>内部复用{@link #entityToFrontVo(Comment, Map)}方法实现单个实体的转换逻辑，避免重复映射配置</p>
     *
     * @param comments   评论实体列表，为null/空列表时返回空列表（非null），无NPE风险
     * @param extraMaps  上下文参数-评论附加信息Map，参数说明同{@link #entityToFrontVo(Comment, Map)}
     * @return 评论VO列表（CommentVO），入参为null/空时返回空列表
     */
    List<CommentVO> entitiesToFrontVos(List<Comment> comments, @Context Map<String, Object> extraMaps);

    /**
     * 从附加信息Map中安全获取评论用户昵称
     *
     * @param comment 评论实体（不可为null）
     * @param extraMaps 评论附加信息Map，为null时返回空字符串
     * @return 作者昵称，无匹配用户时返回空字符串
     */
    @SuppressWarnings("unchecked")
    default String getUserNickname(Comment comment, @Context Map<String, Object> extraMaps) {
        if (extraMaps == null || comment.getUserId() == null) return "";

        Map<Long, UserVO> userIdToUserMap = (Map<Long, UserVO>) extraMaps.get("userIdToUserMap");
        if (userIdToUserMap == null) return "";

        return Optional.ofNullable(userIdToUserMap.get(comment.getUserId()))
                .map(UserVO::getNickname)
                .orElse("");
    }

    /**
     * 从附加信息Map中安全获取评论用户头像
     *
     * @param comment 评论实体（不可为null）
     * @param extraMaps 评论附加信息Map，为null时返回空字符串
     * @return 作者头像，无匹配用户时返回空字符串
     */
    @SuppressWarnings("unchecked")
    default String getUserAvatar(Comment comment, @Context Map<String, Object> extraMaps) {
        if (extraMaps == null || comment.getUserId() == null) return "";

        Map<Long, UserVO> userIdToUserMap = (Map<Long, UserVO>) extraMaps.get("userIdToUserMap");
        if (userIdToUserMap == null) return "";

        return Optional.ofNullable(userIdToUserMap.get(comment.getUserId()))
                .map(UserVO::getAvatar)
                .orElse("");
    }

    /**
     * 从附加信息Map中安全获取评论文章标题
     *
     * @param comment 评论实体（不可为null）
     * @param extraMaps 评论附加信息Map，为null时返回空字符串
     * @return 文章标题，无匹配文章时返回空字符串
     */
    @SuppressWarnings("unchecked")
    default String getArticleTitle(Comment comment, @Context Map<String, Object> extraMaps) {
        if (extraMaps == null || comment.getArticleId() == null) return "";
        Map<Long, String> articleIdToTitleMap = (Map<Long, String>) extraMaps.get("articleIdToTitleMap");
        if (articleIdToTitleMap == null) return "";
        return articleIdToTitleMap.getOrDefault(comment.getArticleId(), "");
    }

    /**
     * 从附加信息Map中安全获取评论的被回复的用户昵称
     *
     * @param comment 评论实体（不可为null）
     * @param extraMaps 评论附加信息Map，为null时返回空字符串
     * @return 被回复的用户昵称，无匹配昵称时返回空字符串
     */
    @SuppressWarnings("unchecked")
    default String getReplyUserNickname(Comment comment, @Context Map<String, Object> extraMaps) {
        if (extraMaps == null || comment.getReplyUserId() == null) return "";
        // 从CommentInfoFillUtil返回的extraMaps中取用户Map
        Map<Long, UserVO> userIdToUserMap = (Map<Long, UserVO>) extraMaps.get("userIdToUserMap");
        if (userIdToUserMap == null) return "";
        // 安全取值：避免NPE，兜底返回空字符串
        return Optional.ofNullable(userIdToUserMap.get(comment.getReplyUserId()))
                .map(UserVO::getNickname)
                .orElse("");
    }

    /**
     * 从附加信息Map中安全获取评论的被回复的评论内容摘要
     *
     * @param comment 评论实体（不可为null）
     * @param extraMaps 评论附加信息Map，为null时返回空字符串
     * @return 被回复的评论内容摘要，无匹配摘要时返回空字符串
     */
    @SuppressWarnings("unchecked")
    default String getReplyContent(Comment comment, @Context Map<String, Object> extraMaps) {
        if (extraMaps == null) return "";
        Map<Long, String> contentMap = (Map<Long, String>) extraMaps.get("replyCommentIdToReplyContentMap");
        if (contentMap == null) return "";

        // 定位目标 ID
        Long targetId = null;

        // 回复子评论 (replyCommentId > 0)
        if (comment.getReplyCommentId() != null && comment.getReplyCommentId() > 0) {
            targetId = comment.getReplyCommentId();
        }
        // 回复顶级评论 (parentId > 0 且此时 replyCommentId 为 0)
        else if (comment.getParentId() != null && comment.getParentId() > 0) {
            targetId = comment.getParentId();
        }

        // 从 Map 中安全提取内容
        if (targetId != null) {
            return contentMap.getOrDefault(targetId, "原评论已删除");
        }
        return "";
    }

}
