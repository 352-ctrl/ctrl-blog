package com.example.blog.common.base;

import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 通用转换接口（抽取所有实体的公共字段转换逻辑）
 * 泛型说明：
 * - E: Entity（数据库实体）
 * - A: AddDTO（新增用DTO）
 * - U: UpdateDTO（修改用DTO）
 * - V: VO（视图对象）
 */
public interface BaseConvert<E, A, U, V> {

    /**
     * 新增DTO → 实体
     * @param addDTO 新增DTO
     * @return 数据库实体
     */
    E addDtoToEntity(A addDTO);

    /**
     * 修改DTO → 实体（更新已有对象）
     * @param updateDTO 修改DTO（源数据）
     * @param entity    已存在的数据库实体（目标数据）
     */
    void updateEntityFromDto(U updateDTO, @MappingTarget E entity);

    /**
     * 实体 → 基础VO
     * @param entity 数据库实体
     * @return 基础视图对象
     */
    V entityToVo(E entity);

    /**
     * 批量转换：实体列表 → 基础VO列表
     * @param entities 实体列表
     * @return 基础VO列表
     */
    List<V> entitiesToVos(List<E> entities);
}
