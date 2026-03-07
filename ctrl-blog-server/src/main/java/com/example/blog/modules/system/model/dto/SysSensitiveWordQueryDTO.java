package com.example.blog.modules.system.model.dto;

import com.example.blog.common.base.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 查询敏感词DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "敏感词分页查询条件")
public class SysSensitiveWordQueryDTO extends PageQueryDTO {

    @Schema(description = "敏感词", example = "雄烯二醇")
    private String word;

}
