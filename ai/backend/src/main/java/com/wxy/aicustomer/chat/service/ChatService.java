package com.wxy.aicustomer.chat.service;

import com.wxy.aicustomer.chat.dto.ChatAnswer;
import com.wxy.aicustomer.chat.dto.ChatEvent;
import com.wxy.aicustomer.chat.dto.ChatRequest;
import com.wxy.aicustomer.chat.dto.MessageVo;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 访客聊天能力。
 */
public interface ChatService {

    Flux<ChatEvent> stream(ChatRequest request);

    ChatAnswer ask(ChatRequest request);

    List<MessageVo> history(String conversationId);

    void clear(String conversationId);
}
