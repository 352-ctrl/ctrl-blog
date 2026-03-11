package com.example.blog.modules.article.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "前台全站搜索结果对象 (VO)", title = "ArticleSearchVO")
public class ArticleSearchVO {
    @Schema(description = "文章ID", type = "string", example = "1623456789012345678")
    private Long id;

    @Schema(
            description = "文章标题 (若搜索引擎支持高亮，此处可能包含 HTML 标签)",
            example = "深入理解 <em>Spring</em> Boot 自动配置"
    )
    private String title;

    @Schema(
            description = "文章摘要/内容片段 (通常截取匹配关键词的前后文)",
            example = "本文将带你深入源码，分析 <em>Spring</em> Boot 是如何实现..."
    )
    private String summary;

    // --- 分类信息 ---

    @Schema(description = "分类ID", type = "string", example = "1623456789012345678")
    private Long categoryId;

    @Schema(description = "分类名称", example = "后端技术")
    private String categoryName;

    // --- 标签信息 ---

    @Schema(description = "文章标签列表")
    @Builder.Default
    private List<TagVO> tags = new ArrayList<>();
}