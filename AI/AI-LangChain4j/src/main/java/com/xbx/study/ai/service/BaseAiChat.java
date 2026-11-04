package com.xbx.study.ai.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface BaseAiChat {


    String chat(Long userId, String prompt);

    String chat(String prompt);

    Flux<String> stream(String sessionId, String prompt);

    Flux<String> stream(String prompt);

}
