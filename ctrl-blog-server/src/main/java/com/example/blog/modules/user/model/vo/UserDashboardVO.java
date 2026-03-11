package com.example.blog.modules.user.model.vo;

import com.example.blog.modules.article.model.vo.ArticleSimpleVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 前台个人中心：总览数据看板 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户总览数据对象")
public class UserDashboardVO {

    @Schema(description = "各项统计数量")
    private Count count;

    @Schema(description = "最近收藏的文章列表 (最多3条)")
    private List<ArticleSimpleVO> recentFavorites;

    @Schema(description = "最近点赞的文章列表 (最多3条)")
    private List<ArticleSimpleVO> recentLikes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Count {
        @Schema(description = "累计收藏数", type = "string")
        private Long favorite;

        @Schema(description = "累计点赞数", type = "string")
        private Long like;

        @Schema(description = "累计评论数", type = "string")
        private Long comment;
    }
}