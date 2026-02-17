package com.example.blog.vo.notice;

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
@Schema(description = "公告展示层对象 (VO)", title = "NoticeVO")
public class NoticeVO {
    @Schema(description = "公告ID", example = "1623456789012345678")
    private Long id;

    @Schema(
            description = "公告内容 (HTML格式)",
            example = "<p>尊敬的用户：<br>系统将于本周日凌晨进行维护...</p>"
    )
    private String contentHtml;

    @Schema(
            description = "是否置顶 (0-不置顶, 1-置顶)",
            example = "1",
            allowableValues = {"0", "1"}
    )
    private Integer isTop;

    @Schema(
            description = "发布时间",
            example = "2023-10-24 10:00:00",
            type = "string",
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}