package com.wxy.zzarental.web.app.controller.chat;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author wxy
 * @description ai 聊天控制器
 * @date 2026/09/09
 */
@Tag(name = "ai管理")
@RestController
@RequestMapping("/app/ai")
public class ChatController {

    @RequestMapping("/chat")
    public Flux<String> chat (String prompt)
}
