package com.example.blog.modules.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.modules.monitor.model.entity.DailyVisit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

@Mapper
public interface VisitMapper extends BaseMapper<DailyVisit> {

    // 1. 原子性增加访问量（存在则+1，不存在则插入）
    @Update("INSERT INTO daily_visit (date, count) VALUES (#{date}, 1) " +
            "ON DUPLICATE KEY UPDATE count = count + 1")
    void incrementVisit(LocalDate date);

    // 2. 统计所有历史访问量（数据库层求和，性能更好）
    @Select("SELECT IFNULL(SUM(count), 0) FROM daily_visit")
    Integer selectTotalVisits();
}