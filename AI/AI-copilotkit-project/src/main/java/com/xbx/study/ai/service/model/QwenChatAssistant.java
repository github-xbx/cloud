package com.xbx.study.ai.service.model;


import dev.langchain4j.service.TokenStream;

public interface QwenChatAssistant {


    /**
     * 流式输出对话
     * @param prompt
     * @return
     */
    TokenStream chat(String prompt);
}
