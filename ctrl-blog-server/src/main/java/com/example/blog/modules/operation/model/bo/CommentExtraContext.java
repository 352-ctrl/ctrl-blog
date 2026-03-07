package com.example.blog.modules.operation.model.bo;

import cn.hutool.core.util.StrUtil;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.modules.user.model.vo.UserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * 评论附加信息上下文
 * 内聚防空指针逻辑，提供类型安全的数据获取方法
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentExtraContext {

    @Builder.Default
    private Map<Long, UserVO> userMap = Collections.emptyMap();

    @Builder.Default
    private Map<Long, String> articleMap = Collections.emptyMap();

    @Builder.Default
    private Map<Long, String> replyContentMap = Collections.emptyMap();

    // ================= 封装安全的数据获取方法，供 MapStruct 直接调用 =================

    public String getUserNickname(Long userId) {
        if (userId == null || !userMap.containsKey(userId)) {
            return StrUtil.EMPTY;
        }
        return userMap.get(userId).getNickname();
    }

    public String getUserAvatar(Long userId) {
        if (userId == null || !userMap.containsKey(userId)) {
            return StrUtil.EMPTY;
        }
        return userMap.get(userId).getAvatar();
    }

    public String getArticleTitle(Long articleId) {
        if (articleId == null || !articleMap.containsKey(articleId)) {
            return StrUtil.EMPTY;
        }
        return articleMap.get(articleId);
    }

    public String getReplyUserNickname(Long replyUserId) {
        if (replyUserId == null || !userMap.containsKey(replyUserId)) {
            return StrUtil.EMPTY;
        }
        return Optional.ofNullable(userMap.get(replyUserId))
                .map(UserVO::getNickname)
                .orElse(StrUtil.EMPTY);
    }

    public String getReplyContent(Long targetId) {
        if (targetId == null || !replyContentMap.containsKey(targetId)) {
            return StrUtil.EMPTY;
        }
        return replyContentMap.getOrDefault(targetId, MessageConstants.MSG_ORIGINAL_COMMENT_DELETED);
    }
}