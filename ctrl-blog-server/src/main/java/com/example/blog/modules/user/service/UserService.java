package com.example.blog.modules.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.modules.user.model.dto.AdminResetPwdDTO;
import com.example.blog.modules.user.model.dto.UserAddDTO;
import com.example.blog.modules.user.model.dto.UserQueryDTO;
import com.example.blog.modules.user.model.dto.UserUpdateDTO;
import com.example.blog.modules.user.model.entity.User;
import com.example.blog.modules.user.model.vo.UserVO;

import java.util.List;

/**
 * 系统用户业务服务接口
 * 定义用户相关的业务操作方法
 */
public interface UserService extends IService<User> {

    /**
     * 后台管理员：强制重置用户密码
     * @param adminResetPwdDTO 重置参数
     */
    void resetPasswordByAdmin(AdminResetPwdDTO adminResetPwdDTO);


    /**
     * 获取单条用户详情
     *
     * @param id 用户ID
     * @return 用户VO
     */
    UserVO getUserById(Long id);

    /**
     * 分页查询用户
     *
     * @param userQueryDTO 查询条件DTO
     * @return 分页结果
     */
    IPage<UserVO> pageAdminUsers(UserQueryDTO userQueryDTO);

    /**
     * 保存用户（包含重复校验和默认值设置）
     *
     * @param userAddDTO 新增用户DTO
     */
    void addUser(UserAddDTO userAddDTO);

    /**
     * 更新用户（包含重复校验）
     *
     * @param userUpdateDTO 更新用户DTO
     */
    void updateUser(UserUpdateDTO userUpdateDTO);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteUserById(Long id);

    /**
     * 批量删除用户
     *
     * @param ids 用户ID列表
     */
    void batchDeleteUsers(List<Long> ids);

}