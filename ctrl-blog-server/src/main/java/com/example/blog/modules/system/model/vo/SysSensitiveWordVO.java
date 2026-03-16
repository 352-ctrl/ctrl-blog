package com.example.blog.modules.system.model.vo;

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
@Schema(description = "敏感词展示层对象 (VO)", title = "TagVO")
public class SysSensitiveWordVO {

    @Schema(description = "敏感词ID", type = "string", example = "1623456789012345678")
    private Long id;

    @Schema(description = "敏感词", example = "雄烯二醇")
    private String word;

    @Schema(
            description = "创建时间",
            example = "2023-10-24 10:24:00",
            type = "string"
    )
    private LocalDateTime createTime;

}