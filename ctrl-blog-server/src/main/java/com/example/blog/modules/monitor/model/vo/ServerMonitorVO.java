package com.example.blog.modules.monitor.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 服务监控数据 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "服务器监控详细数据")
public class ServerMonitorVO {

    @Schema(description = "CPU相关信息")
    private Map<String, Object> cpu;

    @Schema(description = "内存相关信息")
    private Map<String, Object> memory;

    @Schema(description = "JVM相关信息")
    private Map<String, Object> jvm;

    @Schema(description = "服务器信息")
    private Map<String, Object> sys;

    @Schema(description = "磁盘相关信息")
    private Map<String, Object> disk;
}
