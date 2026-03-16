package com.example.blog.modules.article.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "前台文章详情展示对象 (VO)", title = "ArticleDetailVO")
public class ArticleDetailVO {
    @Schema(description = "文章ID", type = "string", example = "1623456789012345678")
    private Long id;

    @Schema(description = "文章封面URL", example = "https://example.com/cover/1.jpg")
    private String cover;

    @Schema(description = "文章标题", example = "Spring Boot 3.0 核心特性解析")
    private String title;

    @Schema(description = "文章摘要 (纯文本/Meta Description)", example = "本文详细介绍了...")
    private String summary;

    @Schema(description = "摘要来源 (0:人工, 1:AI)", example = "0", defaultValue = "0")
    @NotNull(message = "摘要来源不能为空")
    @Range(min = 0, max = 1, message = "摘要来源仅支持 0(人工) 或 1(AI)")
    private Integer isAiSummary;

    @Schema(
            description = "文章正文内容 (HTML格式, 前端需富文本渲染)",
            example = "<p>大家好，今天我们来聊聊 <b>Spring Boot</b>...</p>"
    )
    private String contentHtml;

    // --- 统计数据 ---

    @Schema(description = "浏览量", type = "string", example = "8848")
    private Long viewCount;

    @Schema(description = "点赞数量", type = "string", example = "100")
    private Long likeCount;

    @Schema(description = "当前登录用户是否已点赞", example = "true")
    @Builder.Default
    private boolean liked = false;

    @Schema(description = "收藏数量", type = "string", example = "100")
    private Long favoriteCount;

    @Schema(description = "当前登录用户是否已收藏", example = "true")
    @Builder.Default
    private boolean isFavorite = false;

    @Schema(description = "评论数量 (用于展示 '10条评论')", type = "string", example = "10")
    private Long commentCount;

    @Schema(
            description = "发布时间",
            example = "2023-10-24 10:24:00",
            type = "string"
    )
    private LocalDateTime createTime;

    // --- 分类与作者 ---

    @Schema(description = "分类ID", type = "string", example = "1623456789012345678")
    private Long categoryId;

    @Schema(description = "分类名称", example = "后端技术")
    private String categoryName;

    @Schema(description = "作者ID", type = "string", example = "1623456789012345678")
    private Long userId;

    @Schema(description = "作者昵称", example = "技术宅")
    private String userNickname;

    @Schema(description = "作者头像", example = "https://example.com/avatar.jpg")
    private String userAvatar;

    // --- 标签信息 ---

    @Schema(description = "文章标签列表")
    @Builder.Default
    private List<TagVO> tags = new ArrayList<>();
}