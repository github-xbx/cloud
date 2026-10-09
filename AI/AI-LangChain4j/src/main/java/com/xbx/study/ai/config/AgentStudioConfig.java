package com.xbx.study.ai.config;

import dev.langchain4j.data.message.UserMessage;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.agentexecutor.AgentExecutor;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.studio.LangGraphStudioServer;
import org.bsc.langgraph4j.studio.springboot.LangGraphStudioConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class AgentStudioConfig extends LangGraphStudioConfig {

    private final CompiledGraph<AgentExecutor.State> graph;

    public AgentStudioConfig(@Qualifier("supportAgentGraph") CompiledGraph<AgentExecutor.State> graph) {
        this.graph = graph;
    }


    @Override
    public Map<String, LangGraphStudioServer.Instance> instanceMap() {
        return Map.of("support-agent", LangGraphStudioServer.Instance.builder()
                .title("测试 agent")
                .graph(graph.stateGraph)
                .addInputImageArg("messages", true, v-> UserMessage.from(String.valueOf(v)))
                .compileConfig(CompileConfig.builder().checkpointSaver(new MemorySaver()).build())
                .build());
    }
}
