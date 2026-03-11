package com.example.blog.modules.article.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "前台文章归档展示对象 (VO)", title = "ArticleArchiveVO")
public class ArticleArchiveVO {
    @Schema(description = "文章ID", type = "string", example = "1623456789012345678")
    private Long id;

    @Schema(description = "文章标题", example = "Spring Boot 实战笔记")
    private String title;

    @Schema(
            description = "发布日期",
            example = "2023-10-24",
            type = "string"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createTime;
}