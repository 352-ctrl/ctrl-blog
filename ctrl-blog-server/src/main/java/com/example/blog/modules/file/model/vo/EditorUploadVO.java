package com.example.blog.modules.file.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "WangEditor上传结果响应对象")
public class WangEditorUploadVO {

    @Schema(description = "错误码 (0代表成功，1代表失败)", example = "0")
    private Integer errno;

    @Schema(description = "返回的图片数据 (成功时包含)")
    private List<ImageData> data;

    @Schema(description = "错误提示消息 (失败时包含)", example = "上传失败")
    private String message;

    @Data
    @AllArgsConstructor
    @Schema(description = "图片URL对象")
    public static class ImageData {
        @Schema(description = "图片绝对地址", example = "https://example.com/123.jpg")
        private String url;
    }

    /**
     * 成功响应的快捷构造工厂
     */
    public static WangEditorUploadVO success(String url) {
        return WangEditorUploadVO.builder()
                .errno(0)
                .data(List.of(new ImageData(url)))
                .build();
    }

    /**
     * 失败响应的快捷构造工厂
     */
    public static WangEditorUploadVO fail(String message) {
        return WangEditorUploadVO.builder()
                .errno(1) // WangEditor 规定 1 代表失败
                .message(message)
                .build();
    }
}