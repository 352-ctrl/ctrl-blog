package com.example.blog.modules.system.model.convert;

import com.example.blog.common.base.BaseConvert;
import com.example.blog.modules.system.model.dto.SysJobAddDTO;
import com.example.blog.modules.system.model.dto.SysJobUpdateDTO;
import com.example.blog.modules.system.model.entity.SysJob;
import com.example.blog.common.base.BizStatusTransform;
import com.example.blog.modules.system.model.vo.SysJobVO;
import org.mapstruct.*;

/**
 * 任务专属转换接口（继承通用接口）
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {BaseConvert.class, BizStatusTransform.class} // 关联通用转换接口
)
public interface SysJobConvert extends BaseConvert<SysJob, SysJobAddDTO, SysJobUpdateDTO, SysJobVO> {

    /**
     * 新增DTO → 任务实体
     * @param addDTO 任务新增DTO
     * @return 任务实体
     */
    @Override
    SysJob addDtoToEntity(SysJobAddDTO addDTO);

    /**
     * 修改DTO转换为任务实体
     *
     * @param updateDTO 任务修改DTO，为null时返回null
     * @param sysJob   已存在的数据库实体（目标数据）
     */
    @Override
    // 当源属性为null时，跳过映射，保持目标属性的原值
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(SysJobUpdateDTO updateDTO, @MappingTarget SysJob sysJob);

    /**
     * 任务实体 → 任务VO
     * @param tag 任务实体
     * @return 任务VO
     */
    @Override
    SysJobVO entityToVo(SysJob tag);

}
