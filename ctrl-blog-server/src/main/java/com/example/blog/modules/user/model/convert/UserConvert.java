package com.example.blog.modules.user.model.convert;

import com.example.blog.common.base.BaseConvert;
import com.example.blog.modules.user.model.dto.UserAddDTO;
import com.example.blog.modules.user.model.dto.UserRegisterDTO;
import com.example.blog.modules.user.model.dto.UserUpdateDTO;
import com.example.blog.modules.user.model.entity.User;
import com.example.blog.core.security.PasswordEncoderUtil;
import com.example.blog.common.base.BizStatusTransform;
import com.example.blog.modules.user.model.vo.UserVO;
import org.mapstruct.*;

/**
 * 用户专属转换接口（继承通用接口）
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {BaseConvert.class, BizStatusTransform.class, PasswordEncoderUtil.class} // 关联通用转换接口
)
public interface UserConvert extends BaseConvert<User, UserAddDTO, UserUpdateDTO, UserVO> {

    // ========== 重写/补充用户特有映射规则 ==========
    /**
     * 新增DTO → 用户实体
     * @param addDTO 用户新增DTO
     * @return 用户实体
     */
    @Override
    @Mappings({
            // 若有字段名不一致，可在这里指定：@Mapping(source = "dto字段名", target = "entity字段名")
            // 示例：@Mapping(source = "userName", target = "username")
            @Mapping(target = "password", source = "password", qualifiedByName = "encode")
    })
    User addDtoToEntity(UserAddDTO addDTO);

    /**
     * 修改DTO转换为用户实体
     *
     * @param updateDTO 用户修改DTO，为null时返回null
     * @param user   已存在的数据库实体（目标数据）
     */
    @Override
    // 当源属性为null时，跳过映射，保持目标属性的原值
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UserUpdateDTO updateDTO, @MappingTarget User user);

    /**
     * 新增DTO → 用户实体
     * @param registerDTO 用户新增DTO
     * @return 用户实体
     */
    User registerDtoToEntity(UserRegisterDTO registerDTO);

    /**
     * 用户实体 → 用户VO
     * @param user 用户实体
     * @return 用户VO
     */
    @Override
    UserVO entityToVo(User user);

}
