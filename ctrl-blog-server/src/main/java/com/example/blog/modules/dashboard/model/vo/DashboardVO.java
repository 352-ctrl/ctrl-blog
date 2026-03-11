package com.example.blog.modules.dashboard.model.vo;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "仪表盘聚合数据", title = "DashboardVO")
public class DashboardVO {

    // ================== 1. 顶部统计卡片数据 ==================

    @Schema(description = "文章总数", type = "string", example = "1024")
    private Long articleCount;

    @Schema(description = "评论总数", type = "string", example = "2048")
    private Long commentCount;

    @Schema(description = "用户总数", type = "string", example = "500")
    private Long userCount;

    @Schema(description = "总访问量 (PV)", type = "string", example = "10000")
    private Long visitCount;

    // ================== 2. 图表数据区域 ==================

    @Schema(description = "访问量趋势图数据")
    private VisitTrend visitTrend;

    @Schema(description = "文章分类占比列表")
    private List<CategoryPie> categoryPie;

    // ================== 3. 静态内部类定义 ==================

    /**
     * 内部类：折线图数据
     * 对应前端 ECharts 的 xAxis.data 和 series.data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "访问趋势图数据结构")
    public static class VisitTrend {
        @Schema(description = "日期列表 (X轴)", example = "[\"2023-10-01\", \"2023-10-02\"]")
        private List<String> dates;

        @Schema(description = "访问量列表 (Y轴 PV)", example = "[120, 150]")
        @ArraySchema(schema = @Schema(type = "string", example = "120"))
        private List<Long> pvCounts;
    }

    /**
     * 内部类：饼图数据
     * 对应前端 ECharts 的 {name: '分类A', value: 10}
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "分类占比饼图数据项")
    public static class CategoryPie {
        @Schema(description = "分类名称", example = "Java技术")
        private String name;

        @Schema(description = "文章数量", type = "string", example = "45")
        private Long value;
    }
}
