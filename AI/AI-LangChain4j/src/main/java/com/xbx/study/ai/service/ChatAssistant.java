package com.xbx.study.ai.service;

import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(wiringMode = EXPLICIT,chatModel = "qwen")
public interface ChatAssistant {


    String chat(String prompt);
}
