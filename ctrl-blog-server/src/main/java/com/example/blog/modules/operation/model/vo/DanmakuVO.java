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
@Schema(description = "前台弹幕展示对象 (VO)")
public class DanmakuVO {

    @Schema(description = "弹幕ID")
    private Long id;

    @Schema(description = "弹幕内容")
    private String content;

    @Schema(description = "发送者昵称")
    private String nickname;

    @Schema(description = "发送者头像")
    private String avatar;

    @Schema(description = "发送时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}