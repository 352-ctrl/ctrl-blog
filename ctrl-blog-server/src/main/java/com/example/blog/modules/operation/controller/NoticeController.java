package com.example.blog.modules.operation.controller;

import com.example.blog.common.base.Result;
import com.example.blog.modules.operation.service.NoticeService;
import com.example.blog.modules.operation.model.vo.NoticeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台公告控制器
 * 提供首页公告展示等只读接口
 */
@RestController
@RequestMapping("/api/v1/notices")
@Tag(name = "前台/公告")
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    /**
     * 获取首页滚动公告
     */
    @GetMapping
    @Operation(summary = "获取首页滚动公告", description = "查询状态为“已发布”的公告列表。<br>返回结果优先展示置顶公告，其次按时间倒序排列。通常只返回前 5 条。")
    public Result<List<NoticeVO>> listScrollNotices() {
        List<NoticeVO> notices = noticeService.listScrollNotices();
        return Result.success(notices);
    }

}