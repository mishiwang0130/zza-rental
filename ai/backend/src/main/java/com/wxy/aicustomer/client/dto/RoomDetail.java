package com.wxy.aicustomer.client.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 房源详情。
 */
public record RoomDetail(
        String roomId,
        String apartmentName,
        String roomNumber,
        String floor,
        Integer roomCount,
        BigDecimal rent,
        BigDecimal area,
        String orientation,
        List<String> facilities,
        String description,
        String coverUrl
) {
}
