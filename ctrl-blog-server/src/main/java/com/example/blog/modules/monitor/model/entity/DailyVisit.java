package com.example.blog.modules.monitor.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 每日访问统计实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("blog_daily_visit")
public class DailyVisit {

    /**
     * 主键ID
     * IdType.ASSIGN_ID: 使用雪花算法生成全局唯一ID
     * JsonSerialize: 解决前端 JS 处理 Long 类型精度丢失问题 (转为 String 输出)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate date;

    /**
     * 访问次数
     */
    private Long views;

}