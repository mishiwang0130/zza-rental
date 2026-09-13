package com.wxy.aicustomer.rag;

import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 城市标签过滤条件的构造工具，属于 RAG 检索链路里的"元数据过滤"。
 *
 * <p><b>要解决的问题</b>：知识库里同一类文档按城市分开维护（武汉 / 广州 / 深圳 的租赁规定），
 * 另有平台级通用条款（标签"通用"）。访客在深圳提问时，只应该召回"深圳 + 通用"的片段，
 * 不能让武汉的规定混进参考资料，否则模型会照着别的城市的规定回答。
 *
 * <p><b>三条规则</b>：
 * <ul>
 *   <li>没传城市：返回 {@code null}，表示不加过滤，与不启用城市维度时的行为完全一致；</li>
 *   <li>城市就是"通用"：只按 通用 过滤；</li>
 *   <li>其它城市：按 {@code city in (选中城市, 通用)} 过滤——通用条款在任何城市下都要能被召回。</li>
 * </ul>
 *
 * <p><b>关键点</b>：这个表达式是作为 {@code SearchRequest.filterExpression} 传给 Spring AI 的，
 * 最终会变成 Qdrant search 请求里的 payload 条件，由向量库在检索时一并应用，
 * 而不是"先把 TopK 取回应用层再筛掉"——后者会让结果数量变少甚至为空。
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
