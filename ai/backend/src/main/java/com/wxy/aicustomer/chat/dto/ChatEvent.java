package com.wxy.aicustomer.chat.dto;

/**
 * SSE 事件：事件名 + 载荷。
 */
public record ChatEvent(String name, Object data) {

    public static final String EVENT_META = "meta";
    public static final String EVENT_DELTA = "delta";
    public static final String EVENT_SOURCES = "sources";
    public static final String EVENT_DONE = "done";
    public static final String EVENT_ERROR = "error";

    public static ChatEvent of(String name, Object data) {
        return new ChatEvent(name, data);
    }
}
