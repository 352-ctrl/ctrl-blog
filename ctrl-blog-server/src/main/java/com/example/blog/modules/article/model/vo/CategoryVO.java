package com.example.blog.modules.article.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
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
@Schema(description = "分类展示层对象 (VO)", title = "CategoryVO")
public class CategoryVO {
    @Schema(description = "分类ID", type = "string", example = "1623456789012345678")
    private Long id;

    @Schema(description = "分类名称", example = "后端开发")
    private String name;

    @Schema(
            description = "创建时间",
            example = "2023-10-24 10:24:00",
            type = "string"
    )
    private LocalDateTime createTime;
}