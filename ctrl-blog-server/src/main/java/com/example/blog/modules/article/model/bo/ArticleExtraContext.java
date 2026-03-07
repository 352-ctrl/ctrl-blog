package com.example.blog.modules.article.model.bo;

import cn.hutool.core.util.StrUtil;
import com.example.blog.modules.article.model.vo.TagVO;
import com.example.blog.modules.user.model.vo.UserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 文章附加信息上下文
 * 内聚防空指针逻辑，提供类型安全的数据获取方法
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleExtraContext {

    @Builder.Default
    private Map<Long, UserVO> userMap = Collections.emptyMap();

    @Builder.Default
    private Map<Long, String> categoryMap = Collections.emptyMap();

    @Builder.Default
    private Map<Long, List<TagVO>> tagMap = Collections.emptyMap();

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

    public String getCategoryName(Long categoryId) {
        if (categoryId == null || !categoryMap.containsKey(categoryId)) {
            return StrUtil.EMPTY;
        }
        return categoryMap.get(categoryId);
    }

    public List<TagVO> getArticleTags(Long articleId) {
        if (articleId == null || !tagMap.containsKey(articleId)) {
            return Collections.emptyList();
        }
        return tagMap.get(articleId);
    }
}