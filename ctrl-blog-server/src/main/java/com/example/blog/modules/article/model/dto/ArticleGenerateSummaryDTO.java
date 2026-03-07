package com.example.blog.modules.article.model.dto;

import com.example.blog.core.annotation.CheckSensitive;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI生成摘要请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI生成摘要请求参数")
public class ArticleGenerateSummaryDTO {

    @Schema(description = "文章标题", example = "Spring Boot 3.0 实战", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 255, message = "文章标题长度不能超过255个字符")
    @CheckSensitive(message = "文章标题包含违规词汇，请修改")
    private String title;

    @Schema(description = "文章内容", example = "文章的正文内容...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文章内容不能为空")
    @Size(max = 10000, message = "文章内容过长，无法生成摘要")
    private String content;

}