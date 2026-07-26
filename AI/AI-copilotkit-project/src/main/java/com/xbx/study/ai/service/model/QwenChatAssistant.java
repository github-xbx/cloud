package com.xbx.study.ai.service.model;


import dev.langchain4j.service.TokenStream;
import reactor.core.publisher.Flux;

public interface QwenChatAssistant {


    /**
     * 流式输出对话
     * @param prompt
     * @return
     */
    TokenStream chat(String prompt);

    Flux<String> chat1(String prompt);
}
