package com.xbx.study.ai.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

//@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "qwen1", chatMemoryProvider = )
public interface ChatMemoryAssistant {


    /**
     * 带有缓存功能的 聊天方法
     *
     * @param userId 用户id
     * @param prompt 消息
     * @return
     */
    String chatWithChatMemory(@MemoryId Long userId, @UserMessage String prompt);

}
