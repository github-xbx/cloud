package com.xbx.study.ai.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(wiringMode = EXPLICIT, streamingChatModel = "qwenStream")
public interface StreamChatService extends StreamChat {


    @SystemMessage("你是一个天气助手，只能回答天气问题。")
    @Override
    Flux<String> stream(@UserMessage String prompt);
}
