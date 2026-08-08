package com.xbx.study.ai.service;

import com.xbx.study.ai.tool.BoChaWebSearchTool;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "qwen",
        tools = {"boChaWebSearchTool"}


)
public interface ChatSearchWebAssistant {


    @SystemMessage("你是一个小助手，可以联网查询实时信息。")
    String chat(String prompt);
}
