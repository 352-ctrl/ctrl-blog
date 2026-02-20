package com.example.blog.convert;

import com.example.blog.entity.SysOperLog;
import com.example.blog.vo.SysOperLogVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 系统日志专属转换接口
 */
@Mapper(
        componentModel = "spring"
)
public interface SysOperLogConvert {

    /**
     * 实体转 VO
     */
    SysOperLogVO entityToVo(SysOperLog entity);

    /**
     * 实体列表转 VO 列表
     */
    List<SysOperLogVO> entitiesToVos(List<SysOperLog> entities);

}
