package com.example.blog.modules.system.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "系统操作日志展示层对象 (VO)", title = "SysOperLogVO")
public class SysOperLogVO {

    @Schema(description = "日志主键ID", type = "string", example = "1623456789012345678")
    private Long id;

    @Schema(description = "操作人ID", type = "string", example = "10001")
    private Long userId;

    @Schema(description = "操作人昵称", example = "超级管理员")
    private String nickname;

    @Schema(description = "操作IP", example = "127.0.0.1")
    private String ip;

    @Schema(description = "用户代理 (User-Agent)", example = "Mozilla/5.0 ...")
    private String userAgent;

    @Schema(description = "操作模块", example = "文章管理")
    private String module;

    @Schema(description = "操作类型", example = "新增")
    private String type;

    @Schema(description = "HTTP请求方式", example = "POST")
    private String requestMethod;

    @Schema(description = "操作描述", example = "管理员新增了文章")
    private String description;

    @Schema(description = "请求方法名", example = "com.example.blog.controller.ArticleController.add()")
    private String method;

    @Schema(description = "请求参数")
    private String params;

    @Schema(description = "返回结果或异常信息")
    private String result;

    @Schema(description = "执行耗时 (毫秒)", example = "120")
    private Integer costTime;

    @Schema(description = "操作状态 (1-成功, 0-失败)", allowableValues = {"0", "1"})
    private Integer status;

    @Schema(
            description = "操作时间",
            example = "2023-10-24 10:24:00",
            type = "string"
    )
    private LocalDateTime createTime;
}