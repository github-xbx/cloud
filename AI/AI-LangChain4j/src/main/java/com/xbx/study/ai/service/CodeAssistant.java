package com.xbx.study.ai.service;


import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

/**
 * 代码审查
 */
@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, streamingChatModel = "qwen1",toolProvider = "mcpCodeReviewToolProvider")
public interface CodeAssistant {



    Flux<String> codeReview(String prompt);

}
