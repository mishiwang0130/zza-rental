package com.wxy.aicustomer.client.dto;

import java.time.LocalDateTime;

/**
 * 看房预约请求，对应公寓系统 POST /internal/ai/viewings。
 *
 * <p>requestId 为幂等键：同一访客对同一房源、同一时间重复提交时保持不变。
 */
public record ViewingRequestCommand(
        String requestId,
        String roomId,
        String visitorId,
        String contactName,
        String contactPhone,
        LocalDateTime expectedTime,
        String remark
) {
}
