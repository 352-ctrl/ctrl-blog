package com.example.blog.modules.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.modules.operation.model.dto.EmailRequestDTO;
import com.example.blog.common.base.PageQueryDTO;
import com.example.blog.modules.user.model.dto.UserChangeEmailDTO;
import com.example.blog.modules.user.model.dto.UserChangePwdDTO;
import com.example.blog.modules.user.model.dto.UserProfileUpdateDTO;
import com.example.blog.modules.user.model.vo.UserDashboardVO;
import com.example.blog.modules.article.model.vo.ArticleSimpleVO;
import com.example.blog.modules.operation.model.vo.UserCommentVO;
import com.example.blog.modules.user.model.vo.UserVO;

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
     * 获取用户总览看板数据 (前台)
     *
     * @return 包含统计数据和近期动态的 VO
     */
    UserDashboardVO getUserDashboardData();

    /**
     * 分页获取我的收藏列表
     */
    IPage<ArticleSimpleVO> pageMyFavorites(PageQueryDTO queryDTO);

    /**
     * 分页获取我的点赞列表
     */
    IPage<ArticleSimpleVO> pageMyLikes(PageQueryDTO queryDTO);

    /**
     * 分页获取我的评论列表
     */
    IPage<UserCommentVO> pageMyComments(PageQueryDTO queryDTO);

    /**
     * 更新用户个人信息
     *
     * @param userProfileUpdateDTO 用户信息更新DTO
     */
    void updateProfile(UserProfileUpdateDTO userProfileUpdateDTO);

    /**
     * 发送换绑邮箱的验证码
     *
     * @param emailRequestDTO 邮箱请求DTO
     */
    void sendBindEmailCode(EmailRequestDTO emailRequestDTO);

    /**
     * 更换绑定邮箱
     *
     * @param changeEmailDTO 换绑参数
     * @param token 当前用户的 token (用于强制下线)
     */
    void changeEmail(UserChangeEmailDTO changeEmailDTO, String token);

    /**
     * 更新用户个人密码
     *
     * @param userChangePwdDTO 用户修改密码DTO
     */
    void changePassword(UserChangePwdDTO userChangePwdDTO, String token);


    /**
     * 申请注销当前账号
     *
     * @param token 当前登录用户的Token，用于强制下线
     */
    void requestAccountDeletion(String token);

}
