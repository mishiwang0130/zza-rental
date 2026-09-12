package com.wxy.aicustomer.chat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 聊天接口切片测试：非流式问答、会话历史、清空会话、参数校验。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ChatEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void askShouldReturnAnswerAndConversationId() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"visitorId":"visitor-1","message":"有哪些两室一厅？"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.conversationId").isNotEmpty())
                .andExpect(jsonPath("$.data.answer").isNotEmpty());
    }

    @Test
    void historyAndClearShouldWorkForExistingConversation() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"visitorId":"visitor-2","conversationId":"conv-history","message":"退租要提前多久？"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/chat/conversations/conv-history/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].role").value("user"))
                .andExpect(jsonPath("$.data[1].role").value("assistant"));

        mockMvc.perform(delete("/api/chat/conversations/conv-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/chat/conversations/conv-history/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void blankMessageShouldBeRejectedByCommonExceptionHandler() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"visitorId":"visitor-3","message":"  "}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(202));
    }
}
