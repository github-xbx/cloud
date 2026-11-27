package com.ai.langgraph.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ai 模型 配置类
 *
 */
@Configuration
public class AiModelConfig {


    @Bean(name = "deepseek")
    public ChatModel deepseek(){
        return OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com")
                .apiKey(System.getenv("java_deepseek_apikey"))
                .modelName("deepseek-flash")
                //.httpClientBuilder(new SpringRestClientBuilder())
                .logRequests(true)  //日志级别设置为debug才有效
                //.listeners(List.of(new DeepseekChatModelListener())) //配置监听器,可以配置多个
                .maxRetries(3) //设置重试次数
                //.timeout(Duration.ofSeconds(10)) //设置超时时间 向大模型发送请求时，如果 指定时间内没有收到响应，该请求将被中断并报错 request time out
                .build();
    }



}
