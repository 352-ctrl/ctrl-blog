package com.example.blog.modules.monitor.model.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Redis缓存监控数据 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Redis监控详细数据")
public class RedisMonitorVO {

    @Schema(description = "Redis版本")
    private String version;

    @Schema(description = "运行模式")
    private String runMode;

    @Schema(description = "端口")
    private String port;

    @Schema(description = "运行时间(天)")
    private String uptime;

    @Schema(description = "连接客户端数")
    private String clientCount;

    @Schema(description = "内存配置")
    private String memoryConfig;

    @Schema(description = "AOF是否开启")
    private String aofEnabled;

    @Schema(description = "RDB是否成功")
    private String rdbStatus;

    @Schema(description = "Key数量", type = "string")
    private Long keyCount;

    @Schema(description = "网络入口(kbps)")
    private String networkInput;

    @Schema(description = "网络出口(kbps)")
    private String networkOutput;

    @Schema(description = "键值分页列表")
    private IPage<RedisKeyInfo> keyPage;

    /**
     * 内部类：用于描述键值列表的具体字段
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Redis单个键值信息")
    public static class RedisKeyInfo {
        @Schema(description = "Key键名")
        private String key;

        @Schema(description = "数据类型")
        private String type;

        @Schema(description = "剩余存活时间")
        private String ttl;

        @Schema(description = "大小/数量", type = "string")
        private Long size;
    }
}
