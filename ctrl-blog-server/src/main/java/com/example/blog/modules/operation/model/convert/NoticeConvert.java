package com.example.blog.modules.operation.model.convert;

import com.example.blog.common.base.BaseConvert;
import com.example.blog.modules.operation.model.dto.NoticeAddDTO;
import com.example.blog.modules.operation.model.dto.NoticeUpdateDTO;
import com.example.blog.modules.operation.model.entity.Notice;
import com.example.blog.common.utils.MarkdownUtil;
import com.example.blog.common.base.BizStatusTransform;
import com.example.blog.modules.operation.model.vo.AdminNoticeVO;
import com.example.blog.modules.operation.model.vo.NoticeVO;
import org.mapstruct.*;

/**
 * 公告专属转换接口（继承通用接口）
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {BaseConvert.class, BizStatusTransform.class, MarkdownUtil.class} // 关联通用转换接口
)
public interface NoticeConvert extends BaseConvert<Notice, NoticeAddDTO, NoticeUpdateDTO, NoticeVO> {

    /**
     * 新增DTO → 公告实体
     *
     * @param addDTO 公告新增DTO
     * @return 公告实体
     */
    @Override
    Notice addDtoToEntity(NoticeAddDTO addDTO);

    /**
     * 修改DTO转换为公告实体
     *
     * @param updateDTO 公告修改DTO，为null时返回null
     * @param notice   已存在的数据库实体（目标数据）
     */
    @Override
    // 当源属性为null时，跳过映射，保持目标属性的原值
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(NoticeUpdateDTO updateDTO, @MappingTarget Notice notice);

    /**
     * 公告实体 → 公告VO
     *
     * @param notice 公告实体
     * @return 公告VO
     */
    @Override
    @Mapping(target = "contentHtml", source = "content", qualifiedByName = "markdownToHtml")
    NoticeVO entityToVo(Notice notice);

    /**
     * 公告实体转换为后台公告VO
     *
     * @param notice    公告实体，为null时返回null
     * @return 后台专用-公告VO（entityToAdminVo），实体为null时返回null
     */
    @Mapping(target = "contentHtml", expression = "java(MarkdownUtil.markdownToHtml(notice.getContent()))")
    AdminNoticeVO entityToAdminVo(Notice notice);

    /**
     * 公告实体列表转换为后台公告VO列表
     *
     * @param notice    公告实体列表，为null时返回null
     * @return 后台公告VO列表（entityToAdminVo），实体为null时返回null
     */
    AdminNoticeVO entitiesToAdminVos(Notice notice);

}
