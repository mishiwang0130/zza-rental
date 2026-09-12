package com.wxy.aicustomer.client.dto;

/**
 * 看房预约结果。
 */
public record ViewingResult(String requestId, String appointmentNo, String status, String message) {
}
