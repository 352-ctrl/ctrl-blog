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
@Schema(description = "后台文章管理详情对象 (VO)", title = "AdminArticleVO")
public class AdminArticleVO {
    @Schema(description = "文章ID", type = "string", example = "1623456789012345678")
    private Long id;

    @Schema(description = "文章封面URL", example = "https://example.com/cover.jpg")
    private String cover;

    @Schema(description = "文章标题", example = "Spring Boot 3.0 迁移指南")
    private String title;

    @Schema(description = "文章摘要", example = "本文详细介绍了从 2.7 升级到 3.0 的注意事项...")
    private String summary;

    @Schema(description = "摘要来源 (0:人工, 1:AI)", example = "0", defaultValue = "0")
    @NotNull(message = "摘要来源不能为空")
    @Range(min = 0, max = 1, message = "摘要来源仅支持 0(人工) 或 1(AI)")
    private Integer isAiSummary;

    @Schema(
            description = "文章原始内容 (Markdown格式, 用于编辑器回显)",
            example = "## 1. 前言\n\n升级 Java 17 是第一步..."
    )
    private String content;

    @Schema(description = "浏览量", type = "string", example = "1024")
    private Long viewCount;

    // --- 状态控制字段 ---

    @Schema(
            description = "是否置顶 (0-否, 1-是)",
            example = "0"
    )
    private Integer isTop;

    @Schema(
            description = "是否轮播 (0-否, 1-是)",
            example = "0"
    )
    private Integer isCarousel;

    @Schema(
            description = "发布状态 (0-草稿/隐藏, 1-已发布)",
            example = "1"
    )
    private Integer status;

    @Schema(
            description = "创建时间",
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

    @Schema(description = "作者昵称", example = "Admin")
    private String userNickname;

    // --- 标签信息 ---

    @Schema(description = "文章标签列表")
    @Builder.Default
    private List<TagVO> tags = new ArrayList<>();
}