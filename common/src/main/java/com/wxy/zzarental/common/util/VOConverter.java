package com.wxy.zzarental.common.util;

import cn.hutool.core.bean.BeanUtil;

import java.util.List;

/**
 * 接口 VO 与领域对象之间的简单属性转换器。
 */
public final class VOConverter {

    private VOConverter() {
    }

    public static <S, T> T to(S source, Class<T> targetType) {
        return source == null ? null : BeanUtil.toBean(source, targetType);
    }

    public static <S, T> List<T> toList(List<S> source, Class<T> targetType) {
        return source == null ? null : BeanUtil.copyToList(source, targetType);
    }
}
