package com.example.blog.common.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Level 3: 逻辑删除实体
 * 包含：逻辑删除标识 + 删除时间
 * 适用：核心业务表（文章、用户、评论）
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BaseLogicEntity extends BaseUpdateEntity {

    /**
     * 逻辑删除标识，0表示未删除，1表示已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 删除时间，逻辑删除时自动填充
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime deleteTime;

}
