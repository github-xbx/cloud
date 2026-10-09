package com.xbx.study.ai.config;

import com.xbx.study.ai.service.OrderToolService;
import com.xbx.study.ai.service.WeatherToolService;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.agentexecutor.AgentExecutor;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 一个基于 Langgraph4j AgentExecutor 的 ReAct Agent
 *
 * 图结构 有 AgentExecutor 内部生成
 * __START__ -> agent(掉模型) -> 有 tool_calls -> action(执行工具) -> agent ... -> __END__
 * 你不需要手写节点/边 只需要：模型 + 工具 + 系统提示词
 */
@Configuration
public class AgentConfiguration {


    /**
     * 会话记忆： savers 按 threadId 存 checkpoint
     * 注： 这里用的是内存，生产换 Postgres/MySQL/Redis saver
     * @return
     */
    @Bean
    public MemorySaver agentMemorySaver(){
        return new MemorySaver();
    }


    /**
     * 同步版
     * @param qwen 模型
     * @param agentMemorySaver 存储
     * @param weatherToolService tool
     * @param orderToolService tool
     * @return
     * @throws GraphStateException
     */
    @Bean("supportAgentGraph")
    public CompiledGraph<AgentExecutor.State> supportAgentGraph(
            @Qualifier("qwen")ChatModel qwen,
            MemorySaver agentMemorySaver,
            WeatherToolService weatherToolService,
            OrderToolService orderToolService) throws GraphStateException {

        return AgentExecutor.builder()
                .chatModel(qwen)
                .systemMessage(SystemMessage.from("""
                        你是电商客服助手。回答规则:
                        1. 需要订单状态、退款、发短信、天气等信息时,必须先调用对应工具,不要凭猜测回答;
                        2. 工具返回什么就基于什么回答,不要编造物流单号或时间;
                        3. 一次只做用户要求的事;信息不足时先向用户追问;
                        4. 用简洁的中文回答,不要暴露工具名和内部实现。
                        """))
                .toolsFromObject(orderToolService)
                .toolsFromObject(weatherToolService)
                .build()
                .compile(CompileConfig.builder()
                        .checkpointSaver(agentMemorySaver)
                        .releaseThread(false)      // ← 关键:跑完不释放 thread,checkpoint 才能留到下一轮
                        .build()
                );

    }

    /**
     * 流式版
     * AgentExecutor 里 ChatModel 和 StreamingChatModel 是互斥的
     * 所以另键一个 Bean 两个Bean 公用同一个 MemorySaver， ThreadId相同就记忆相同
     * @param qwenStreaming
     * @param agentMemorySaver
     * @param weatherToolService
     * @param orderToolService
     * @return
     * @throws GraphStateException
     */
    @Bean("supportAgentStreamGraph")
    public CompiledGraph<AgentExecutor.State> supportAgentStreamGraph(
            @Qualifier("qwen1") StreamingChatModel qwenStreaming,
            MemorySaver agentMemorySaver,
            WeatherToolService weatherToolService,
            OrderToolService orderToolService) throws GraphStateException {

        return AgentExecutor.builder()
                .chatModel(qwenStreaming,true)
                .systemMessage(SystemMessage.from("你是电商客服助手,需要外部信息必须先调用工具,用简洁中文回答。"))
                .toolsFromObject(orderToolService)
                .toolsFromObject(weatherToolService)
                .build()
                .compile(CompileConfig.builder()
                        .checkpointSaver(agentMemorySaver)
                        .releaseThread(false)      // ← 关键:跑完不释放 thread,checkpoint 才能留到下一轮
                        .build()
                );

    }


}
