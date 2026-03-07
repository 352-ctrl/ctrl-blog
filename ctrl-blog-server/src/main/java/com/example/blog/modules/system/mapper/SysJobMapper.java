package com.example.blog.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.modules.system.model.entity.SysJob;
import org.apache.ibatis.annotations.Mapper;

/**
 * 定时任务调度数据访问层
 * 处理系统定时任务的增删改查操作
 * 对应实体类：SysJob
 * 对应数据库表：sys_job
 *
 * @see SysJob
 * @see BaseMapper
 */
@Mapper
public interface SysJobMapper extends BaseMapper<SysJob> {
}
