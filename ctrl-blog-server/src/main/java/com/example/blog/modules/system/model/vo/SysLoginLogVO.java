package com.example.blog.modules.system.model.vo;

import com.example.blog.common.jackson.EmailDesensitizeSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
@Schema(description = "登录日志展示层对象 (VO)", title = "SysLoginLogVO")
public class SysLoginLogVO {

    @Schema(description = "日志ID", type = "string", example = "1623456789012345678")
    private Long id;

    @JsonSerialize(using = EmailDesensitizeSerializer.class)
    @Schema(description = "登录账号(邮箱)", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "登录IP地址", example = "127.0.0.1")
    private String ip;

    @Schema(description = "登录地点", example = "中国 广东省 深圳市")
    private String location;

    @Schema(description = "浏览器类型", example = "Chrome 118")
    private String browser;

    @Schema(description = "操作系统", example = "Windows 11")
    private String os;

    @Schema(
            description = "登录状态 (1-成功, 0-失败)",
            example = "1",
            allowableValues = {"0", "1"}
    )
    private Integer status;

    @Schema(description = "操作提示信息", example = "登录成功")
    private String message;

    @Schema(
            description = "登录时间",
            example = "2023-10-24 10:24:00",
            type = "string"
    )
    private LocalDateTime createTime;
}