package com.xbx.study.ai.service;

import reactor.core.publisher.Flux;


public interface StreamingChatAssistant {

    /**
     * 流式输出
     * @param prompt
     * @return
     */
    Flux<String> chatFlux(String prompt);
}
