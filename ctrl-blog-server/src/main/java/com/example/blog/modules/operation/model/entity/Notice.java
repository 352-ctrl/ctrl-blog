package com.example.blog.modules.operation.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.blog.common.enums.BizStatus;
import com.example.blog.common.base.BaseUpdateEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 系统公告实体类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
public class Notice extends BaseUpdateEntity {

    /**
     * 公告内容
     */
    private String content;

    /**
     * 状态 1:发布 0:草稿
     */
    private Integer status;

    /**
     * 是否置顶：1-是，0-否
     */
    @TableField("is_top")
    private BizStatus.Common isTop;

}