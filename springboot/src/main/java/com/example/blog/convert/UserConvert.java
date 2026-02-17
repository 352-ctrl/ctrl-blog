package com.example.blog.convert;

import cn.hutool.core.util.DesensitizedUtil;
import com.example.blog.dto.user.UserAddDTO;
import com.example.blog.dto.user.UserRegisterDTO;
import com.example.blog.dto.user.UserUpdateDTO;
import com.example.blog.entity.User;
import com.example.blog.utils.PasswordEncoderUtil;
import com.example.blog.vo.UserVO;
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
    @Mappings({
            @Mapping(source = "email", target = "email"),
    })
    User registerDtoToEntity(UserRegisterDTO registerDTO);

    /**
     * 用户实体 → 用户VO
     * @param user 用户实体
     * @return 用户VO
     */
    @Override
    @Mappings({
            // 邮箱脱敏：调用自定义的desensitizeEmail方法处理
            @Mapping(target = "email", qualifiedByName = "desensitizeEmail"),
    })
    UserVO entityToVo(User user);

    /**
     * 邮箱脱敏转换器
     * 邮箱脱敏：123456@qq.com → 123****@qq.com
     */
    @Named("desensitizeEmail")
    default String desensitizeEmail(String email) {
        return DesensitizedUtil.email(email);
    }
}
