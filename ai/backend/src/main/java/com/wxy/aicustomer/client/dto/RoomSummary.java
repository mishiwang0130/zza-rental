package com.wxy.aicustomer.client.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 房源列表项。
 */
public record RoomSummary(
        String roomId,
        String apartmentName,
        String roomNumber,
        Integer roomCount,
        BigDecimal rent,
        BigDecimal area,
        List<String> labels
) {
}
