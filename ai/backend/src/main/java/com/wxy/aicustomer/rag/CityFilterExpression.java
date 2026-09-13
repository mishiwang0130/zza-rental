package com.wxy.aicustomer.rag;

import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 城市标签过滤条件。
 *
 * <p>选中城市时按"选中城市 + 通用"过滤，让平台级通用文档在任何城市下都能被召回；
 * 未选择城市时返回 {@code null}，表示检索不带任何过滤，与不启用城市维度的行为一致。
 */
public final class CityFilterExpression {

    private CityFilterExpression() {
    }

    public static Filter.Expression build(String city, String cityMetadataKey, String commonCity) {
        if (!StringUtils.hasText(city)) {
            return null;
        }
        String selected = city.trim();
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        if (selected.equals(commonCity)) {
            return builder.eq(cityMetadataKey, selected).build();
        }
        return builder.in(cityMetadataKey, List.of(selected, commonCity)).build();
    }
}
