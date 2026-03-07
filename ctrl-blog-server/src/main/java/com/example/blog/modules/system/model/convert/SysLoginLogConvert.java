package com.example.blog.modules.system.model.convert;

import com.example.blog.modules.system.model.entity.SysLoginLog;
import com.example.blog.modules.system.model.vo.SysLoginLogVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 登录日志专属转换接口
 */
@Mapper(
        componentModel = "spring"
)
public interface SysLoginLogConvert {

    /**
     * 实体转 VO
     */
    SysLoginLogVO entityToVo(SysLoginLog entity);

    /**
     * 实体列表转 VO 列表
     */
    List<SysLoginLogVO> entitiesToVos(List<SysLoginLog> entities);

}
