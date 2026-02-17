package com.example.blog.vo.article;

import com.example.blog.vo.TagVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "前台文章列表展示对象 (VO)", title = "ArticleListVO")
public class ArticleCardVO {
    @Schema(description = "文章ID", example = "1623456789012345678")
    @JsonSerialize(contentUsing = ToStringSerializer.class)
    private Long id;

    @Schema(description = "文章封面URL", example = "https://example.com/cover/1.jpg")
    private String cover;

    @Schema(description = "文章标题", example = "Spring Boot 3.0 核心特性解析")
    private String title;

    @Schema(description = "文章摘要", example = "本文详细介绍了 Spring Boot 3.0 的 AOT 编译...")
    private String summary;

    @Schema(description = "浏览量", example = "1024")
    private Long viewCount;

    // --- 状态标识 ---

    @Schema(
            description = "是否置顶 (0-否, 1-是)",
            example = "1",
            allowableValues = {"0", "1"}
    )
    private Integer isTop;

    @Schema(
            description = "是否推荐/轮播 (0-否, 1-是)",
            example = "0",
            allowableValues = {"0", "1"}
    )
    private Integer isCarousel;

    @Schema(
            description = "发布时间",
            example = "2023-10-24 10:24:00",
            type = "string",
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    // --- 分类与作者 ---

    @Schema(description = "分类ID", example = "1623456789012345678")
    @JsonSerialize(contentUsing = ToStringSerializer.class)
    private Long categoryId;

    @Schema(description = "分类名称", example = "后端技术")
    private String categoryName;

    @Schema(description = "作者ID", example = "1623456789012345678")
    @JsonSerialize(contentUsing = ToStringSerializer.class)
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