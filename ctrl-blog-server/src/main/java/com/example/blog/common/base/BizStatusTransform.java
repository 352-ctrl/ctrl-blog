package com.example.blog.common.base;

import com.example.blog.common.enums.BaseEnum;
import org.mapstruct.Mapper;
import org.mapstruct.TargetType;

import java.util.Arrays;

/**
 * 万能枚举转换器
 * 利用泛型自动处理所有 BaseEnum 子类的转换，无需为每个枚举单独写方法
 */
@Mapper(componentModel = "spring")
public interface BizStatusTransform {

    /* ================= 1. Integer <-> Enum (适用于 Common, User, Article, JobStatus 等) ================= */

    /**
     * Integer 转 任意枚举
     * MapStruct 会自动检测目标类型，并将目标 Enum 的 Class 传给 @TargetType 参数
     */
    default <E extends Enum<E> & BaseEnum<Integer>> E integerToEnum(Integer code, @TargetType Class<E> enumClass) {
        if (code == null) {
            return null;
        }
        // 利用流遍历目标枚举的所有实例
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.getValue().equals(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 任意枚举 转 Integer
     * 只要实现了 BaseEnum<Integer> 接口的枚举，都会走这个方法
     */
    default Integer enumToInteger(BaseEnum<Integer> enumObj) {
        return enumObj == null ? null : enumObj.getValue();
    }

    /* ================= 2. String <-> Enum (适用于 Role, JobGroup 等) ================= */

    /**
     * String 转 任意枚举
     */
    default <E extends Enum<E> & BaseEnum<String>> E stringToEnum(String code, @TargetType Class<E> enumClass) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.getValue().equals(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 任意枚举 转 String
     */
    default String enumToString(BaseEnum<String> enumObj) {
        return enumObj == null ? null : enumObj.getValue();
    }
}