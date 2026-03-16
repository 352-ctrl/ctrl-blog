package com.example.blog.modules.file.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import com.example.blog.common.base.Result;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.core.annotation.AuthCheck;
import com.example.blog.core.annotation.Log;
import com.example.blog.core.annotation.RateLimit;
import com.example.blog.core.exception.CustomerException;
import com.example.blog.modules.file.model.vo.EditorUploadVO;
import com.example.blog.modules.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理控制器
 * 处理文件的上传（本地/OSS）和下载操作
 */
@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "通用公共接口/文件服务")
public class FileController {

    @Resource
    private FileService fileService;

    /**
     * 将前端的业务类型映射为后端安全的目录常量
     */
    private String getDirByType(String type) {
        if (StrUtil.isBlank(type)) {
            return null; // 默认传根目录或公共目录
        }
        return switch (type.toLowerCase()) {
            case "avatar" -> Constants.UPLOAD_DIR_AVATAR; // 常量：avatar
            case "cover" -> Constants.UPLOAD_DIR_COVER;   // 常量：cover
            case "article" -> Constants.UPLOAD_DIR_ARTICLE; // 常量：article
            default -> throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_INVALID_UPLOAD_TYPE);
        };
    }

    /**
     * 通用文件上传 (POST /api/v1/files)
     */
    @PostMapping
    @AuthCheck
    @RateLimit(key = "ip", time = 60, count = 10)
    @Log(module = "文件服务", type = "上传", desc = "通用文件上传")
    @Operation(summary = "文件上传", description = "上传单文件。根据系统配置的存储策略（本地/云存储），自动返回完整的资源访问绝对 URL。")
    public Result<String> upload(
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "业务类型(avatar/cover/article等)", required = false)
            @RequestParam(value = "type", required = false) String type) {

        String targetDir = getDirByType(type);
        String url = fileService.upload(file, targetDir);
        return Result.success(url);
    }

    /**
     * 文件下载 (GET /api/v1/files/{fileName})
     * 注意：此接口返回流，不返回 JSON Result
     */
    @GetMapping("/{fileName}")
    @Operation(summary = "文件下载 (⚠️云存储模式下禁用)",
            description = "根据文件名下载文件流。<br/>" +
                    "**注意：** 当系统启用 OSS 云存储（如七牛云）时，此接口会被禁用并抛出异常。前端需直接使用上传时返回的 CDN 绝对路径进行图片展示或下载。")
    public void download(@Parameter(description = "文件名") @PathVariable String fileName, HttpServletResponse response) {
        fileService.download(fileName, response);
    }

    /**
     * WangEditor 编辑器专用上传 (POST /api/v1/files/wang)
     * 格式要求：{ "errno": 0, "data": [{ "url": "..." }] }
     */
    @PostMapping("/wang")
    @AuthCheck(role = BizStatus.ROLE_ADMIN)
    @RateLimit(key = "ip", time = 60, count = 20)
    @Log(module = "文件服务", type = "上传", desc = "编辑器图片上传")
    @Operation(summary = "编辑器文件上传", description = "WangEditor 编辑器专用接口，返回特定 JSON 格式。")
    public EditorUploadVO uploadForEditor(@RequestParam("file") MultipartFile file) {
        try {
            String url = fileService.upload(file, Constants.UPLOAD_DIR_ARTICLE);

            // 组装 WangEditor 兼容格式
            Object data = CollUtil.newArrayList(Dict.create().set("url", url));
            return EditorUploadVO.success(data);
        } catch (Exception e) {
            // 返回带常量的失败信息
            return EditorUploadVO.fail(MessageConstants.MSG_UPLOAD_FAILURE);
        }
    }
}