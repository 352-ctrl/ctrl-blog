package com.example.blog.convert;

import com.example.blog.dto.tag.TagAddDTO;
import com.example.blog.dto.tag.TagUpdateDTO;
import com.example.blog.entity.Tag;
import com.example.blog.vo.TagVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 标签专属转换接口（继承通用接口）
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = BaseConvert.class // 关联通用转换接口
)
public interface TagConvert extends BaseConvert<Tag, TagAddDTO, TagUpdateDTO, TagVO> {

    /**
     * 新增DTO → 标签实体
     * @param addDTO 标签新增DTO
     * @return 标签实体
     */
    @Override
    Tag addDtoToEntity(TagAddDTO addDTO);

    /**
     * 标签实体 → 标签VO
     * @param tag 标签实体
     * @return 标签VO
     */
    @Override
    TagVO entityToVo(Tag tag);

}
