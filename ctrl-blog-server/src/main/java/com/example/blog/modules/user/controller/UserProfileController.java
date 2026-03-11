package com.example.blog.modules.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.core.annotation.Log;
import com.example.blog.core.annotation.RateLimit;
import com.example.blog.core.annotation.VerifyCaptcha;
import com.example.blog.common.base.Result;
import com.example.blog.common.constants.Constants;
import com.example.blog.modules.operation.model.dto.EmailRequestDTO;
import com.example.blog.common.base.PageQueryDTO;
import com.example.blog.modules.user.model.dto.UserChangeEmailDTO;
import com.example.blog.modules.user.model.dto.UserChangePwdDTO;
import com.example.blog.modules.user.model.dto.UserProfileUpdateDTO;
import com.example.blog.modules.user.service.UserProfileService;
import com.example.blog.modules.user.model.vo.UserDashboardVO;
import com.example.blog.modules.article.model.vo.ArticleSimpleVO;
import com.example.blog.modules.operation.model.vo.UserCommentVO;
import com.example.blog.modules.user.model.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 前台个人中心控制器
 * 处理当前登录用户的个人信息查询、修改及安全设置
 */
@RestController
@RequestMapping("/api/v1/profile")
@AuthCheck
@Tag(name = "前台/个人中心")
public class UserProfileController {

    @Resource
    private UserProfileService userProfileService;

    /**
     * 获取当前用户信息
     */
    @GetMapping
    @Operation(summary = "获取个人信息", description = "获取当前登录用户的详细档案信息（昵称、头像、邮箱等）。")
    public Result<UserVO> getProfile() {
        UserVO userVO = userProfileService.getProfile();
        return Result.success(userVO);
    }

    /**
     * 获取个人中心总览数据
     */
    @GetMapping("/dashboard")
    @Operation(summary = "获取个人中心看板数据", description = "返回用户的点赞/收藏统计及最近互动文章列表。")
    public Result<UserDashboardVO> getUserDashboardData() {
        UserDashboardVO dashboardVO = userProfileService.getUserDashboardData();
        return Result.success(dashboardVO);
    }

    /**
     * 修改个人信息
     */
    @PutMapping
    @RateLimit(key = "ip", time = 60, count = 5)
    @Log(module = "个人中心", type = "修改", desc = "用户修改了个人信息")
    @Operation(summary = "修改个人信息", description = "更新昵称、头像、简介等基本资料。")
    public Result<Void> updateProfile(@Valid @RequestBody UserProfileUpdateDTO updateDTO) {
        userProfileService.updateProfile(updateDTO);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    @RateLimit(key = "ip", time = 60, count = 3) // 关键安全接口，限流严格一点
    @Log(module = "个人中心", type = "安全", desc = "用户执行了修改密码操作")
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码。修改成功后通常建议引导用户重新登录。")
    public Result<Void> changePassword(@Valid @RequestBody UserChangePwdDTO pwdDTO, HttpServletRequest request) {
        // 从 Header 获取 Token
        String token = request.getHeader(Constants.HEADER_TOKEN);
        userProfileService.changePassword(pwdDTO, token);
        return Result.success();
    }

    @PostMapping("/email/verification-codes")
    @VerifyCaptcha
    @RateLimit(key = "ip", time = 60, count = 1)
    @Operation(summary = "发送换绑邮箱验证码", description = "向新邮箱发送验证码，需通过行为验证码校验。")
    public Result<Void> sendBindEmailCode(@Valid @RequestBody EmailRequestDTO emailRequestDTO) {
        userProfileService.sendBindEmailCode(emailRequestDTO);
        return Result.success();
    }

    /**
     * 确认更换邮箱
     */
    @PutMapping("/email")
    @RateLimit(key = "ip", time = 60, count = 3)
    @Log(module = "个人中心", type = "安全", desc = "用户执行了换绑邮箱操作")
    @Operation(summary = "更换绑定邮箱", description = "校验验证码并换绑。换绑成功后当前账号会被强制下线。")
    public Result<Void> changeEmail(@Valid @RequestBody UserChangeEmailDTO changeEmailDTO, HttpServletRequest request) {
        // 从 Header 获取 Token，用于强制下线
        String token = request.getHeader(Constants.HEADER_TOKEN);
        userProfileService.changeEmail(changeEmailDTO, token);
        return Result.success();
    }

    /**
     * 获取我的收藏列表
     */
    @GetMapping("/favorites")
    @Operation(summary = "我的收藏", description = "分页获取当前用户收藏的文章列表")
    public Result<IPage<ArticleSimpleVO>> pageMyFavorites(@Valid @ModelAttribute PageQueryDTO queryDTO) {
        return Result.success(userProfileService.pageMyFavorites(queryDTO));
    }

    /**
     * 获取我的点赞列表
     */
    @GetMapping("/likes")
    @Operation(summary = "我的点赞", description = "分页获取当前用户点赞的文章列表")
    public Result<IPage<ArticleSimpleVO>> pageMyLikes(@Valid @ModelAttribute PageQueryDTO queryDTO) {
        return Result.success(userProfileService.pageMyLikes(queryDTO));
    }

    /**
     * 获取我的评论列表
     */
    @GetMapping("/comments")
    @Operation(summary = "我的评论", description = "分页获取当前用户发布的评论列表")
    public Result<IPage<UserCommentVO>> pageMyComments(@Valid @ModelAttribute PageQueryDTO queryDTO) {
        return Result.success(userProfileService.pageMyComments(queryDTO));
    }

    /**
     * 申请注销账号
     */
    @PostMapping("/cancel")
    @RateLimit(key = "ip", time = 60, count = 1)
    @Log(module = "个人中心", type = "安全", desc = "用户申请注销了账号")
    @Operation(summary = "申请注销账号", description = "将账号状态改为注销冷静期，并强制下线。冷静期内再次登录可撤销。")
    public Result<Void> requestAccountDeletion(HttpServletRequest request) {
        String token = request.getHeader(Constants.HEADER_TOKEN);
        userProfileService.requestAccountDeletion(token);
        return Result.success();
    }
}