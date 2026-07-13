package com.xbx.study.ai.config;

import com.xbx.study.ai.listener.DeepseekChatModelListener;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilder;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class LLMConfiguration {


    /**
     * 定义 deepseek chat model
     * @return DeepSeek ChatModel
     */
    @Bean(name = "deepseek")
    public ChatModel deepseek(){
        return OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com")
                .apiKey(System.getenv("java_deepseek_apikey"))
                .modelName("deepseek-v4-pro")
                .httpClientBuilder(new SpringRestClientBuilder())
                .logRequests(true)  //日志级别设置为debug才有效
                .listeners(List.of(new DeepseekChatModelListener())) //配置监听器,可以配置多个
                .maxRetries(3) //设置重试次数
                .timeout(Duration.ofSeconds(10)) //设置超时时间 向大模型发送请求时，如果 指定时间内没有收到响应，该请求将被中断并报错 request time out
                .build();
    }


    @Bean(name = "qwen")
    public ChatModel qwen(){
        return OpenAiChatModel.builder()
                .baseUrl("https://ws-2gcnpdewhflb89dx.cn-beijing.maas.aliyuncs.com/compatible-mode/v1")
                .apiKey(System.getenv("java_qwen_apikey"))
                .modelName("qwen3.5-omni-plus-2026-03-15")
                .httpClientBuilder(new SpringRestClientBuilder())
                .build();
    }

}
