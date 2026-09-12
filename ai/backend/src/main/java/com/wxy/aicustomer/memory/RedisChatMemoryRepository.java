package com.wxy.aicustomer.memory;

import com.wxy.aicustomer.common.constant.AiRedisKeys;
import com.wxy.zzarental.common.util.RedisUtil;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的匿名会话存储。
 *
 * <p>每个 conversationId 对应一个 Redis List，保存最近若干轮消息；写入时整体覆盖，
 * 因为 saveAll 传入的已经是完整窗口。Redis 操作复用 common 的 RedisUtil。
 */
public class RedisChatMemoryRepository implements ChatMemoryRepository {

    private final RedisUtil redisUtil;
    private final MessageCodec messageCodec;
    private final Duration ttl;

    public RedisChatMemoryRepository(RedisUtil redisUtil, MessageCodec messageCodec, Duration ttl) {
        this.redisUtil = redisUtil;
        this.messageCodec = messageCodec;
        this.ttl = ttl;
    }

    @Override
    public List<String> findConversationIds() {
        Set<String> members = redisUtil.setMembers(AiRedisKeys.CHAT_CONVERSATION_IDS);
        return members == null ? List.of() : members.stream().sorted().toList();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        List<String> payloads = redisUtil.listRange(memoryKey(conversationId), 0, -1);
        if (payloads == null || payloads.isEmpty()) {
            return List.of();
        }
        return payloads.stream().map(messageCodec::decode).filter(Objects::nonNull).toList();
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        String key = memoryKey(conversationId);
        redisUtil.delete(key);
        if (messages == null || messages.isEmpty()) {
            return;
        }
        redisUtil.rightPushAll(key, messages.stream().map(messageCodec::encode).toList());
        redisUtil.expire(key, ttl.toMillis(), TimeUnit.MILLISECONDS);
        redisUtil.setAdd(AiRedisKeys.CHAT_CONVERSATION_IDS, conversationId);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        redisUtil.delete(memoryKey(conversationId));
        redisUtil.setRemove(AiRedisKeys.CHAT_CONVERSATION_IDS, conversationId);
    }

    private String memoryKey(String conversationId) {
        return AiRedisKeys.CHAT_MEMORY_PREFIX + conversationId;
    }
}
