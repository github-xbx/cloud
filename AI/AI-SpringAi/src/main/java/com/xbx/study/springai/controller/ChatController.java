package com.xbx.study.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("ai")
public class ChatController {


    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @RequestMapping(value = "/chat", produces = "text/html; charset=utf-8")
    public Flux<String> chat(@RequestParam(value = "prompt") String prompt){
        return chatClient
                .prompt()
                .user(prompt)
                .stream()
                .content();
    }

}
