package com.example.blog.modules.operation.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "新增弹幕请求参数")
public class DanmakuAddDTO {

    @Schema(description = "弹幕内容", example = "博主牛逼！", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "弹幕内容不能为空")
    @Size(max = 100, message = "弹幕内容长度不能超过100字")
    private String content;

}