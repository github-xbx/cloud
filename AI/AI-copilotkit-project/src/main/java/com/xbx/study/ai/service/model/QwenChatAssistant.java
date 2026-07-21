package com.xbx.study.ai.service.model;


import reactor.core.publisher.Flux;

public interface QwenChatAssistant {


    /**
     * 流式输出对话
     * @param prompt
     * @return
     */
    Flux<String> chat(String prompt);
}
