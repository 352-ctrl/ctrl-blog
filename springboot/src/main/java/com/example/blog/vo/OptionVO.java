package com.example.blog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下拉框/字典通用 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通用下拉选项键值对")
public class OptionVO<T> {

    @Schema(description = "选项值 (传给后端的值)", example = "DEFAULT")
    private T value;

    @Schema(description = "选项标签 (展示给用户的文本)", example = "默认分组")
    private String label;
}
