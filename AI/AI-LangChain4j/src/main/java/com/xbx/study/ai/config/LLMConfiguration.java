package com.xbx.study.ai.config;

import com.xbx.study.ai.listener.DeepseekChatModelListener;
import com.xbx.study.ai.service.ChatMemoryAssistant;
import com.xbx.study.ai.service.LawAssistant;
import com.xbx.study.ai.service.StreamingChatAssistant;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                //.httpClientBuilder(new SpringRestClientBuilder())
                .logRequests(true)  //日志级别设置为debug才有效
                .listeners(List.of(new DeepseekChatModelListener())) //配置监听器,可以配置多个
                .maxRetries(3) //设置重试次数
                //.timeout(Duration.ofSeconds(10)) //设置超时时间 向大模型发送请求时，如果 指定时间内没有收到响应，该请求将被中断并报错 request time out
                .build();
    }


    @Bean(name = "qwen")
    public ChatModel qwen(){
        return OpenAiChatModel.builder()
                .baseUrl("https://ws-2gcnpdewhflb89dx.cn-beijing.maas.aliyuncs.com/compatible-mode/v1")
                .apiKey(System.getenv("java_qwen_apikey"))
                .modelName("qwen3.5-omni-plus-2026-03-15")
                //.httpClientBuilder(new SpringRestClientBuilder())
                .build();
    }
    @Bean(name = "qwen1")
    public StreamingChatModel qwen1(){
        return OpenAiStreamingChatModel.builder()
                .baseUrl("https://ws-2gcnpdewhflb89dx.cn-beijing.maas.aliyuncs.com/compatible-mode/v1")
                .apiKey(System.getenv("java_qwen_apikey"))
                .modelName("qwen3-vl-235b-a22b-thinking")
                .logRequests(true)
                .logResponses(true)
                .build();
    }


    @Bean(name = "qwenImageModel")
    public WanxImageModel imageModel(){
        return WanxImageModel.builder()
                .baseUrl("https://ws-2gcnpdewhflb89dx.cn-beijing.maas.aliyuncs.com/api/v1" )
                .apiKey(System.getenv("java_qwen_apikey"))
                //.modelName("qwen-image-2.0-pro-2026-06-22")
                .modelName("wanx2.0-t2i-turbo")
                //.logRequests(true)

                .build();
    }


    /**
     * 不使用 @AiService 注解方式定义 ai service bean
     * @param streamingChatModel
     * @return
     */
    @Bean
    public StreamingChatAssistant streamingChatAssistant(@Qualifier("qwen1") StreamingChatModel streamingChatModel){
        return AiServices.create(StreamingChatAssistant.class, streamingChatModel);
    }


    /**
     * 基于消息缓存的 chatAssistant
     * @param chatModel
     * @return
     */
    @Bean(name = "chatMemoryMessageWindows")
    public ChatMemoryAssistant chatMemoryMessageWindows(@Qualifier("qwen") ChatModel chatModel){
        return AiServices.builder(ChatMemoryAssistant.class)
                .chatModel(chatModel)
                // 注意每个memoryId 对应创建一个ChatMemory
                .chatMemoryProvider(memoryId -> {
                   return MessageWindowChatMemory.withMaxMessages(100); //最大消息数 100
                })
                .build();
    }

    /**
     * 基于token缓存的 chatAssistant
     * @param chatModel
     * @return
     */
    @Bean(name = "chatMemoryTokenWindows")
    public ChatMemoryAssistant chatMemoryTokenWindows(@Qualifier("qwen") ChatModel chatModel){
        // TokenCountEstimator 默认的token 分词器，需要杰克 Tokenizer 计算 ChatMessage的token数量
        OpenAiTokenCountEstimator openAiTokenizer = new OpenAiTokenCountEstimator("gpt-4.");
        return AiServices.builder(ChatMemoryAssistant.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> {
                    return TokenWindowChatMemory.withMaxTokens(1000, openAiTokenizer); //最大1000个token
                })
                .build();
    }


    /**
     * 存在提示词的 chatModel
     * @param chatModel
     * @return
     */
    @Bean(name = "lawAssistant")
    public LawAssistant lawAssistant(@Qualifier("qwen") ChatModel chatModel){
        // TokenCountEstimator 默认的token 分词器，需要杰克 Tokenizer 计算 ChatMessage的token数量
        OpenAiTokenCountEstimator openAiTokenizer = new OpenAiTokenCountEstimator("gpt-4.");
        return AiServices.builder(LawAssistant.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> {
                    return TokenWindowChatMemory.withMaxTokens(1000, openAiTokenizer); //最大1000个token
                })
                .build();
    }


}
