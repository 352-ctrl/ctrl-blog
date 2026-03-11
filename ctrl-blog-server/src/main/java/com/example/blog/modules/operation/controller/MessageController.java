package com.example.blog.modules.operation.controller;

import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.common.base.Result;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.modules.operation.service.MessageService;
import com.example.blog.modules.operation.model.vo.MessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台系统消息控制器
 * 提供用户个人的未读消息数获取、消息已读标记等接口
 */
@RestController
@RequestMapping("/api/v1/messages")
@Tag(name = "前台/消息中心")
@AuthCheck
public class MessageController {

    @Resource
    private MessageService MessageService;

    /**
     * 获取个人的消息列表 (最多返回200条)
     */
    @GetMapping("/list")
    @Operation(summary = "获取消息列表", description = "一次性获取当前用户的消息数据，最多返回最近的200条。支持按类型过滤。")
    public Result<List<MessageVO>> listMessages(
            @Parameter(description = "消息大类(可选)，不传则获取所有")
            @RequestParam(value = "type", required = false) BizStatus.MessageType type) {

        List<MessageVO> messageVOS = MessageService.listMessages(type);
        return Result.success(messageVOS);
    }

    /**
     * 获取未读消息总数
     */
    @GetMapping("/unread/count")
    @Operation(summary = "获取未读消息总数", description = "获取当前登录用户的未读消息总数，通常用于全局导航栏的小红点展示。")
    public Result<Integer> getUnreadMessageCount() {
        Integer count = MessageService.getUnreadMessageCount();
        return Result.success(count);
    }

    /**
     * 标记单条消息为已读
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "标记单条消息为已读", description = "用户点击具体某条未读消息时，将其状态更新为已读。")
    public Result<Void> markMessageAsRead(
            @Parameter(description = "消息的主键ID") @PathVariable("id") Long id) {
        MessageService.markMessageAsRead(id);
        return Result.success();
    }

    /**
     * 一键全部已读
     */
    @PutMapping("/read-status")
    @Operation(summary = "一键全部已读", description = "将当前用户所有的未读消息标记为已读，支持按消息大类(type)进行分类一键已读。")
    public Result<Void> markAllAsRead(
            @Parameter(description = "消息大类(可选)，不传则清空所有类型的未读") @RequestParam(value = "type", required = false) BizStatus.MessageType type) {
        MessageService.markAllAsRead(type);
        return Result.success();
    }

}