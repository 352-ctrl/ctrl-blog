package com.example.blog.modules.operation.model.vo;

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
@Schema(description = "个人中心-我的评论列表展示对象", title = "UserCommentVO")
public class UserCommentVO {

    @Schema(description = "评论ID", type = "string", example = "1623456789012345678")
    private Long id;

    @Schema(description = "评论内容", example = "博主写得太好了，受益匪浅！")
    private String content;

    @Schema(
            description = "评论时间",
            example = "2023-10-24 10:24:00",
            type = "string"
    )
    private LocalDateTime createTime;

    @Schema(description = "所属文章ID", type = "string", example = "111222333444")
    private Long articleId;

    @Schema(description = "所属文章标题", example = "Spring Boot 3.0 核心特性解析")
    private String articleTitle;

}