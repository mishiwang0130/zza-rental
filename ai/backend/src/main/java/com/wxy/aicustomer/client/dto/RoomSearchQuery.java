package com.wxy.aicustomer.client.dto;

import java.math.BigDecimal;

/**
 * 查询空房条件，对应公寓系统 POST /internal/ai/rooms/search。
 */
public record RoomSearchQuery(
        String cityCode,
        BigDecimal minRent,
        BigDecimal maxRent,
        Integer roomCount,
        Integer minArea,
        Integer maxArea,
        Integer limit
) {
}
