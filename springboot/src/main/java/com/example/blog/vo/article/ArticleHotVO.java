package com.example.blog.vo.article;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "前台文章列表展示对象 (VO)", title = "ArticleHotVO")
public class ArticleHotVO {

    @Schema(description = "文章ID", example = "1623456789012345678")
    @JsonSerialize(contentUsing = ToStringSerializer.class)
    private Long id;

    @Schema(description = "文章封面URL", example = "https://example.com/cover/1.jpg")
    private String cover;

    @Schema(description = "文章标题", example = "Spring Boot 3.0 核心特性解析")
    private String title;

    @Schema(
            description = "发布时间",
            example = "2023-10-24 10:24:00",
            type = "string",
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

}