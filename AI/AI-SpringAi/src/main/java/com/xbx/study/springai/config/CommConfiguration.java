package com.xbx.study.springai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommConfiguration {


    /**
     * ChatClient
     */
    @Bean
    public ChatClient client(OpenAiChatModel model){

        return ChatClient
                .builder(model)

                .defaultAdvisors(new SimpleLoggerAdvisor())   // 打印每次请求/响应，调试神器
                .build();
    }

}
