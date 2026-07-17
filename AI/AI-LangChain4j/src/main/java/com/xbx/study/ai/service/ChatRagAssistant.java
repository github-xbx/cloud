package com.xbx.study.ai.service;


import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * contentRetriever 是 LangChain4j 的 AiServices 中用于实现 RAG（检索增强生成） 功能的核心属性。
 * 简单来说，它的作用是在调用 AI 模型之前，自动从一个外部数据源（比如向量数据库、搜索引擎等）中，
 * 检索与当前用户问题最相关的内容，并将这些内容作为上下文信息，一起发送给大语言模型 (LLM)
 */
@AiService(wiringMode = EXPLICIT, chatModel = "qwen", chatMemory = "messageMemory", contentRetriever = "contentRetriever")
public interface ChatRagAssistant {

    String chat(String prompt);
}
