package com.example.blog.modules.operation.model.dto;

import com.example.blog.common.base.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 后台查询评论DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "后台评论分页查询条件")
public class AdminCommentQueryDTO extends PageQueryDTO {

    @Schema(description = "评论人昵称 (模糊查询)", example = "张三")
    private String userNickname;

    @Schema(description = "文章标题 (模糊查询)", example = "Spring Boot")
    private String articleTitle;

}