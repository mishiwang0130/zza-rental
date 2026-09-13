package com.wxy.aicustomer.common.constant;

import com.wxy.zzarental.common.constant.RedisKeyConstant;

/**
 * AI 客服的 Redis Key 约定。
 *
 * <p>复用 common 的 RedisKeyConstant.PREFIX_KEY，与公寓系统共用同一套 key 前缀规范。
 */
public final class AiRedisKeys {

    /** 会话消息列表：zza:ai:chat:memory:{conversationId} */
    public static final String CHAT_MEMORY_PREFIX = RedisKeyConstant.PREFIX_KEY + "ai:chat:memory:";

    /** 会话 ID 索引集合，用于列出全部匿名会话 */
    public static final String CHAT_CONVERSATION_IDS = RedisKeyConstant.PREFIX_KEY + "ai:chat:conversations";

    private AiRedisKeys() {
    }
}
