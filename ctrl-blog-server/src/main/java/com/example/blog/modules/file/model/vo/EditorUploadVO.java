package com.example.blog.modules.file.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "富文本编辑器上传返回对象 (适配 WangEditor)")
public class EditorUploadVO {

    @Schema(description = "错误码，0 代表成功，非 0 代表失败", example = "0")
    private Integer errno;

    @Schema(description = "错误提示，仅在 errno 非 0 时需要", example = "上传失败")
    private String message;

    @Schema(description = "返回数据载荷，包含图片 url 列表或对象")
    private Object data;

    /**
     * 快捷构建成功响应
     */
    public static EditorUploadVO success(Object data) {
        return EditorUploadVO.builder()
                .errno(0)
                .data(data)
                .build();
    }

    /**
     * 快捷构建失败响应
     */
    public static EditorUploadVO fail(String message) {
        return EditorUploadVO.builder()
                .errno(1) // WangEditor 规定非 0 为失败
                .message(message)
                .build();
    }
}