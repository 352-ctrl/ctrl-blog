package com.example.blog.modules.article.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文章归档聚合 VO (按年份分组)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "归档列表对象 (VO)", title = "ArchiveVO")
public class ArchiveAggregateVO {

    @Schema(description = "年份", example = "2023")
    private Integer year;

    @Schema(description = "该年份的文章数量", example = "5")
    private Integer count;

    @Schema(description = "文章列表")
    private List<ArticleArchiveVO> articles;
}
