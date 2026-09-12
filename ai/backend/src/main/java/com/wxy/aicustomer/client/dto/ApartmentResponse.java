package com.wxy.aicustomer.client.dto;

/**
 * 公寓系统统一响应结构，字段与公寓系统 Result 保持一致。
 */
public record ApartmentResponse<T>(int code, String message, T data) {

    private static final int SUCCESS_CODE = 200;

    public boolean isSuccess() {
        return code == SUCCESS_CODE;
    }
}
