package com.example.blog.modules.article.model.dto;

import com.example.blog.core.annotation.CheckSensitive;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;

import java.util.ArrayList;
import java.util.List;

/**
 * 更新文章DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新文章请求参数")
public class ArticleUpdateDTO {

    @Schema(description = "文章ID", example = "1623456789012345678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "文章ID不能为空")
    @Positive(message = "文章ID必须为正整数")
    private Long id;

    @Schema(description = "文章封面URL", example = "https://example.com/new-cover.jpg")
    @Size(max = 500, message = "封面URL长度不能超过500个字符")
    private String cover;

    @Schema(description = "文章标题", example = "Spring Boot 3.0 实战教程 (修订版)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 255, message = "文章标题长度不能超过255个字符")
    @CheckSensitive(message = "文章标题包含违规词汇，请修改")
    private String title;

    @Schema(description = "文章摘要", example = "本文增加了关于Knife4j的配置说明...")
    @NotBlank(message = "文章摘要不能为空")
    @Size(max = 255, message = "文章摘要长度不能超过255个字符")
    @CheckSensitive(message = "文章摘要包含违规词汇，请修改")
    private String summary;

    @Schema(description = "摘要来源 (0:人工, 1:AI)", example = "0", defaultValue = "0")
    @NotNull(message = "摘要来源不能为空")
    @Range(min = 0, max = 1, message = "摘要来源仅支持 0(人工) 或 1(AI)")
    private Integer isAiSummary;

    @Schema(description = "文章内容", example = "更新后的内容...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文章内容不能为空")
    private String content;

    @Schema(description = "是否置顶 (0:否, 1:是)", example = "0")
    @NotNull(message = "置顶状态不能为空")
    @Range(min = 0, max = 1, message = "置顶状态仅支持 0(否) 或 1(是)")
    private Integer isTop;

    @Schema(description = "是否轮播 (0:否, 1:是)", example = "0")
    @NotNull(message = "轮播配置不能为空")
    @Range(min = 0, max = 1, message = "轮播状态仅支持 0(否) 或 1(是)")
    private Integer isCarousel;

    @Schema(description = "文章状态 (0:草稿, 1:已发布)", example = "1")
    @NotNull(message = "文章状态不能为空")
    @Range(min = 0, max = 1, message = "文章状态仅支持 0(草稿) 或 1(已发布)")
    private Integer status;

    @Schema(description = "分类ID", example = "1623456789012345678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "分类ID不能为空")
    @Positive(message = "分类ID必须为正整数")
    private Long categoryId;

    @Schema(description = "标签ID列表", example = "[\"1623456789012345678\", \"1623456789012345679\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "标签ID不能为空")
    @Size(max = 3, message = "标签ID数量不能大于3个")
    @JsonSerialize(contentUsing = ToStringSerializer.class)
    @Builder.Default
    private List<@Positive(message = "标签ID必须为正整数") Long> tagIds = new ArrayList<>();

}