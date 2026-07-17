package com.xbx.study.ai.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(wiringMode = EXPLICIT, chatModel = "qwen", chatMemoryProvider = "tokensMemory", tools = "weatherToolService")
public interface FunctionHighAssistant {


    String chat(@MemoryId Long memoryId, @UserMessage String prompt);
}
