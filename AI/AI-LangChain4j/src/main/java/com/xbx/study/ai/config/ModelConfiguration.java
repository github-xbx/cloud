package com.xbx.study.ai.config;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.utils.Constants;
import com.xbx.study.ai.listener.DeepseekChatModelListener;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ModelConfiguration {

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
                .modelName("qwen3.7-max-2026-06-08")
                //.httpClientBuilder(new SpringRestClientBuilder())
                .build();
    }
    @Bean(name = "qwen1")
    public StreamingChatModel qwen1(){
        return OpenAiStreamingChatModel.builder()
                .baseUrl("https://ws-2gcnpdewhflb89dx.cn-beijing.maas.aliyuncs.com/compatible-mode/v1")
                .apiKey(System.getenv("java_qwen_apikey"))
                .modelName("qwen3.7-max-2026-06-08")
                //.logRequests(true)
                //.logResponses(true)
                .build();
    }


    @Bean(name = "qwenImageModel")
    public WanxImageModel imageModel(){
        return WanxImageModel.builder()
                .baseUrl("https://ws-2gcnpdewhflb89dx.cn-beijing.maas.aliyuncs.com/api/v1" )
                .apiKey(System.getenv("java_qwen_apikey"))
                .modelName("qwen-image-2.0-pro-2026-06-22")
                //.modelName("wan2.7-t2v-2026-06-12")
                //.logRequests(true)

                .build();
    }





    /**
     * 向量模型
     * @return
     */
    @Bean(name = "qwen_embedding")
    public EmbeddingModel embeddingModel(){
        return OpenAiEmbeddingModel.builder()
                .baseUrl("https://ws-2gcnpdewhflb89dx.cn-beijing.maas.aliyuncs.com/compatible-mode/v1")
                .modelName("qwen3.7-text-embedding")
                .apiKey(System.getenv("java_qwen_apikey"))
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
