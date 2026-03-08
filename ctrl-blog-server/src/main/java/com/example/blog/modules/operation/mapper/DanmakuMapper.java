package com.example.blog.modules.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.modules.operation.model.entity.Danmaku;
import org.apache.ibatis.annotations.Mapper;

/**
 * 弹幕数据访问层
 * 对应实体类：Danmaku
 * 对应数据库表：blog_danmaku
 */
@Mapper
public interface DanmakuMapper extends BaseMapper<Danmaku> {

}