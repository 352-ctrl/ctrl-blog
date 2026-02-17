package com.example.blog.service;

import com.example.blog.dto.user.UserChangePwdDTO;
import com.example.blog.dto.user.UserProfileUpdateDTO;
import com.example.blog.vo.UserVO;

/**
 * 个人信息业务服务接口
 * 定义个人信息相关的业务操作方法
 */
public interface UserProfileService {

    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    UserVO getProfile();

    /**
     * 更新用户个人信息
     *
     * @param userProfileUpdateDTO 用户信息更新DTO
     */
    void updateProfile(UserProfileUpdateDTO userProfileUpdateDTO);

    /**
     * 更新用户个人密码
     *
     * @param userChangePwdDTO 用户修改密码DTO
     */
    void changePassword(UserChangePwdDTO userChangePwdDTO, String token);

}
