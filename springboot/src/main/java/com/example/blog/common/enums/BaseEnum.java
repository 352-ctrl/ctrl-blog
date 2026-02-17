package com.example.blog.common.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

import java.io.Serializable;

/**
 * 顶级枚举接口
 * T: 数据库存储的类型 (Integer/String)
 */
public interface BaseEnum<T extends Serializable> extends IEnum<T> {

    // 1. 获取数据库存储的值
    @Override
    T getValue();

    // 2. 获取前端显示的描述
    String getDesc();

}
