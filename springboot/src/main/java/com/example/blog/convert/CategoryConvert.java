package com.example.blog.convert;

import com.example.blog.dto.category.CategoryAddDTO;
import com.example.blog.dto.category.CategoryUpdateDTO;
import com.example.blog.entity.Category;
import com.example.blog.vo.CategoryVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 分类专属转换接口（继承通用接口）
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = BaseConvert.class // 关联通用转换接口
)
public interface CategoryConvert extends BaseConvert<Category, CategoryAddDTO, CategoryUpdateDTO, CategoryVO> {

    /**
     * 新增DTO → 分类实体
     * @param addDTO 分类新增DTO
     * @return 分类实体
     */
    @Override
    Category addDtoToEntity(CategoryAddDTO addDTO);

    /**
     * 分类实体 → 分类VO
     * @param category 分类实体
     * @return 分类VO
     */
    @Override
    CategoryVO entityToVo(Category category);

}
