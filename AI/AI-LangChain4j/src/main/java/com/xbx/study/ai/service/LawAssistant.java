package com.xbx.study.ai.service;

import com.xbx.study.ai.po.prompt.LawPrompt;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface LawAssistant {


    /**
     * @SystemMessage 系统提示词，用于明确定义助手的角色和你那里范围，
     * @UserMessage 用户提示词 整合@V 可以精确控制输入和期望的输出格式，确保问题被正确回答
     * @MemoryId 缓存id 用于不同用户之间的对话的缓存隔离
     * @param question
     * @param length
     * @return
     */
    @SystemMessage("你是一位专业的中国法律顾问，只回答与中国法律相关的问题。"  +
           "输出限制：对于其他领域的问题禁止回答，直接返回'抱歉，我只能回答中国法律相关的问题。'")
    @UserMessage("请回答一下法律问题：{{question}}, 字数控制待{{length}}以内")
    String chat(@MemoryId Long userId,@V("question") String question, @V("length") int length);


    @SystemMessage("你是一位专业的中国法律顾问，只回答与中国法律相关的问题。"  +
            "输出限制：对于其他领域的问题禁止回答，直接返回'抱歉，我只能回答中国法律相关的问题。'")
    String chat(LawPrompt prompt);

}
