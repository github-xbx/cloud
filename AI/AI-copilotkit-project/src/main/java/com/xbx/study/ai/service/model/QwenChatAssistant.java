package com.xbx.study.ai.service.model;



import dev.langchain4j.invocation.InvocationParameters;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface QwenChatAssistant {


    /**
     * 流式输出对话
     * @param prompt
     * @return
     */
    TokenStream chat(String prompt);

    /**
     * 流式输出对话 带附加属性
     * @param prompt
     * @param parameters //附加属性可以在listener中拿到
     * @return
     */
    TokenStream chat(@UserMessage String prompt, InvocationParameters parameters);
}
