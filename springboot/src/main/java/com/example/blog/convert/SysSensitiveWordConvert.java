package com.example.blog.convert;

import com.example.blog.dto.word.SysSensitiveWordAddDTO;
import com.example.blog.dto.word.SysSensitiveWordUpdateDTO;
import com.example.blog.entity.SysSensitiveWord;
import com.example.blog.vo.SysSensitiveWordVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 敏感词专属转换接口（继承通用接口）
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = BaseConvert.class // 关联通用转换接口
)
public interface SysSensitiveWordConvert extends BaseConvert<SysSensitiveWord, SysSensitiveWordAddDTO, SysSensitiveWordUpdateDTO, SysSensitiveWordVO> {

    /**
     * 新增DTO → 敏感词实体
     * @param addDTO 敏感词新增DTO
     * @return 敏感词实体
     */
    @Override
    SysSensitiveWord addDtoToEntity(SysSensitiveWordAddDTO addDTO);

    /**
     * 敏感词实体 → 敏感词VO
     * @param sysSensitiveWord 敏感词实体
     * @return 敏感词VO
     */
    @Override
    SysSensitiveWordVO entityToVo(SysSensitiveWord sysSensitiveWord);

}