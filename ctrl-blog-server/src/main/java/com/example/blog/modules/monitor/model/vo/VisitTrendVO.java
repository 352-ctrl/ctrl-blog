package com.example.blog.modules.monitor.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@Schema(description = "访问量趋势图数据VO")
public class VisitTrendVO {
    @Schema(description = "日期列表 (X轴)", example = "[\"2023-10-01\", \"2023-10-02\"]")
    private List<String> dates;

    @Schema(description = "访问量列表 (Y轴 PV)", example = "[120, 150]")
    private List<Long> pvCounts;
}