package com.wxy.aicustomer.memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxy.zzarental.common.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Redis 会话仓库测试。
 *
 * <p>用内存实现替换 common 的 RedisUtil，既验证读写语义，又不依赖真实 Redis。
 */
class RedisChatMemoryRepositoryTest {

    private final FakeRedisUtil redisUtil = new FakeRedisUtil();
    private RedisChatMemoryRepository repository;

    @BeforeEach
    void setUp() {
        redisUtil.clear();
        repository = new RedisChatMemoryRepository(redisUtil, new MessageCodec(new ObjectMapper()),
                Duration.ofDays(1));
    }

    @Test
    void shouldSaveAndReadConversationMessages() {
        repository.saveAll("conv-1", List.of(new UserMessage("你好"), new AssistantMessage("你好，有什么可以帮你？")));

        List<Message> messages = repository.findByConversationId("conv-1");

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getMessageType()).isEqualTo(MessageType.USER);
        assertThat(messages.get(1).getText()).isEqualTo("你好，有什么可以帮你？");
        assertThat(repository.findConversationIds()).containsExactly("conv-1");
    }

    @Test
    void saveAllShouldReplacePreviousMessages() {
        repository.saveAll("conv-2", List.of(new UserMessage("第一轮")));
        repository.saveAll("conv-2", List.of(new UserMessage("第二轮"), new AssistantMessage("回答")));

        assertThat(repository.findByConversationId("conv-2")).hasSize(2);
    }

    @Test
    void deleteShouldRemoveMessagesAndConversationId() {
        repository.saveAll("conv-3", List.of(new UserMessage("待删除")));

        repository.deleteByConversationId("conv-3");

        assertThat(repository.findByConversationId("conv-3")).isEmpty();
        assertThat(repository.findConversationIds()).isEmpty();
    }

    @Test
    void saveEmptyMessagesShouldNotCreateList() {
        repository.saveAll("conv-4", List.of());

        assertThat(redisUtil.lists).doesNotContainKey("zza:ai:chat:memory:conv-4");
        assertThat(repository.findConversationIds()).isEmpty();
    }

    /**
     * RedisUtil 的内存替身，只实现会话仓库用到的方法。
     */
    private static class FakeRedisUtil extends RedisUtil {

        private final Map<String, List<String>> lists = new ConcurrentHashMap<>();
        private final Set<String> conversationIds = new LinkedHashSet<>();

        void clear() {
            lists.clear();
            conversationIds.clear();
        }

        @Override
        public List<String> listRange(String key, long start, long end) {
            return List.copyOf(lists.getOrDefault(key, List.of()));
        }

        @Override
        public Boolean delete(String key) {
            return lists.remove(key) != null;
        }

        @Override
        public Long rightPushAll(String key, Collection<String> values) {
            lists.computeIfAbsent(key, ignored -> new ArrayList<>()).addAll(values);
            return (long) lists.get(key).size();
        }

        @Override
        public Boolean expire(String key, long timeout, TimeUnit unit) {
            return true;
        }

        @Override
        public Long setAdd(String key, String... values) {
            int before = conversationIds.size();
            conversationIds.addAll(List.of(values));
            return (long) (conversationIds.size() - before);
        }

        @Override
        public Long setRemove(String key, Object... values) {
            int before = conversationIds.size();
            List.of(values).forEach(value -> conversationIds.remove(String.valueOf(value)));
            return (long) (before - conversationIds.size());
        }

        @Override
        public Set<String> setMembers(String key) {
            return Set.copyOf(conversationIds);
        }
    }
}
