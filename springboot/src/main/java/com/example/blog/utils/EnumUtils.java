package com.example.blog.utils;

import com.example.blog.common.enums.BaseEnum;
import com.example.blog.vo.OptionVO;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 枚举工具类
 */
public class EnumUtils {

    /**
     * 将实现了 BaseEnum 接口的枚举转换为下拉框列表 (OptionVO)
     *
     * @param enumClass 枚举类的 Class 对象 (例如: BizStatus.JobGroup.class)
     * @param <T>       枚举值的类型 (Integer 或 String)
     * @param <E>       枚举类型本身
     * @return 包含 value 和 label 的列表
     */
    public static <T extends Serializable, E extends Enum<E> & BaseEnum<T>> List<OptionVO<T>> toOptions(Class<E> enumClass) {
        // 使用流式编程，一行代码搞定转换
        return Arrays.stream(enumClass.getEnumConstants())
                .map(e -> new OptionVO<>(e.getValue(), e.getDesc()))
                .collect(Collectors.toList());
    }
}
