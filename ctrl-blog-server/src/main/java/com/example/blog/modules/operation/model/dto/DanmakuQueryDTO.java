package com.example.blog.modules.operation.model.dto;

import com.example.blog.common.base.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "弹幕分页查询条件")
public class DanmakuQueryDTO extends PageQueryDTO {

    @Schema(description = "弹幕内容 (模糊查询)", example = "好漂亮")
    private String content;

    @Schema(description = "发送者昵称 (模糊查询)", example = "游客")
    private String nickname;

}