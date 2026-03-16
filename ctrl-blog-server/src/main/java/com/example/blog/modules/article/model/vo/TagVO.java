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
@Schema(description = "标签展示层对象 (VO)", title = "TagVO")
public class TagVO {
    @Schema(description = "标签ID", type = "string", example = "1623456789012345678")
    private Long id;

    @Schema(description = "标签名称", example = "Spring Boot")
    private String name;

    @Schema(
            description = "创建时间",
            example = "2023-10-24 10:24:00",
            type = "string"
    )
    private LocalDateTime createTime;
}