package com.example.blog.controller.front;

import com.example.blog.common.Result;
import com.example.blog.service.TagService;
import com.example.blog.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台标签控制器
 * 提供标签列表、标签云展示等只读接口
 */
@RestController
@RequestMapping("/api/front/tags")
@Tag(name = "前台标签")
public class TagController {

    @Resource
    private TagService tagService;

    /**
     * 获取前台展示用的标签列表
     */
    @GetMapping
    @Operation(summary = "获取前台展示用的标签列表", description = "获取前台展示用的标签列表。<br>返回数据通常用于首页“标签云”展示、侧边栏或文章筛选。")
    public Result<List<TagVO>> listPortalTags() {
        List<TagVO> tags = tagService.listPortalTags();
        return Result.success(tags);
    }

}