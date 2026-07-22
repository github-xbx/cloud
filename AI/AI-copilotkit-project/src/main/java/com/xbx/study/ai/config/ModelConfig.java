package com.xbx.study.ai.config;

import com.xbx.study.ai.service.model.QwenChatAssistant;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ModelConfig {

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
                //.httpClientBuilder(new SpringRestClientBuilder())
                .logRequests(true)  //日志级别设置为debug才有效
                //.listeners(List.of(new DeepseekChatModelListener())) //配置监听器,可以配置多个
                .maxRetries(3) //设置重试次数
                //.timeout(Duration.ofSeconds(10)) //设置超时时间 向大模型发送请求时，如果 指定时间内没有收到响应，该请求将被中断并报错 request time out
                .build();
    }


    @Bean(name = "deepseek_stream")
    public StreamingChatModel deepseekStream(){
        return OpenAiStreamingChatModel.builder()
                .baseUrl("https://api.deepseek.com")
                .apiKey(System.getenv("java_deepseek_apikey"))
                .modelName("deepseek-v4-pro")
                .logRequests(true)
                .logResponses(true)
                .reasoningEffort("high")
                .returnThinking(true)  // 启用思考内容接收
                .build();
    }


    /**
     * 千问 3.5
     * @return
     */
    @Bean(name = "qwen")
    public ChatModel qwen(){
        return OpenAiChatModel.builder()
                .baseUrl("https://ws-2gcnpdewhflb89dx.cn-beijing.maas.aliyuncs.com/compatible-mode/v1")
                .apiKey(System.getenv("java_qwen_apikey"))
                .modelName("qwen3.5-omni-plus-2026-03-15")
                //.httpClientBuilder(new SpringRestClientBuilder())
                .build();
    }


    /**
     * 千问3 流式输出 model chat
     * @return
     */
    @Bean(name = "qwen3")
    public StreamingChatModel qwen1(){
        return OpenAiStreamingChatModel.builder()
                .baseUrl("https://ws-2gcnpdewhflb89dx.cn-beijing.maas.aliyuncs.com/compatible-mode/v1")
                .apiKey(System.getenv("java_qwen_apikey"))
                .modelName("qwen3-vl-235b-a22b-thinking")
                .logRequests(true)
                .logResponses(true)
                .returnThinking(true)  // 关键：启用 reasoning_content → onPartialThinking 的路由
                .build();
    }



    @Bean
    public QwenChatAssistant streamingChatAssistant(@Qualifier("qwen3") StreamingChatModel streamingChatModel){
        return AiServices.create(QwenChatAssistant.class, streamingChatModel);
    }



}
