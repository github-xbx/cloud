package com.ai.langgraph.config;

import com.ai.langgraph.service.langchain.DeployLLMService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LLMBeanConfig {


    @Bean
    public DeployLLMService deployLLMService(@Qualifier("deepseek") ChatModel deepseek){
        return AiServices.builder(DeployLLMService.class)
                .chatModel(deepseek)
                // 注意每个memoryId 对应创建一个ChatMemory

                .build();
    }


}
