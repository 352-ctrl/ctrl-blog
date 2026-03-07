package com.example.blog.modules.monitor.model.dto;

import com.example.blog.common.base.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Redis键值查询条件")
public class RedisKeyQueryDTO extends PageQueryDTO {

    @Schema(description = "键名关键字(模糊搜索)")
    private String keyword;
}