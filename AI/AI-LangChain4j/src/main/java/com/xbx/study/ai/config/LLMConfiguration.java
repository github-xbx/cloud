package com.xbx.study.ai.config;

import com.xbx.study.ai.listener.DeepseekChatModelListener;
import com.xbx.study.ai.service.ChatMemoryAssistant;
import com.xbx.study.ai.service.FunctionAssistant;
import com.xbx.study.ai.service.LawAssistant;
import com.xbx.study.ai.service.StreamingChatAssistant;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

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
                .modelName("qwen3.7-max-2026-05-20")
                //.httpClientBuilder(new SpringRestClientBuilder())
                .build();
    }
    @Bean(name = "qwen1")
    public StreamingChatModel qwen1(){
        return OpenAiStreamingChatModel.builder()
                .baseUrl("https://ws-2gcnpdewhflb89dx.cn-beijing.maas.aliyuncs.com/compatible-mode/v1")
                .apiKey(System.getenv("java_qwen_apikey"))
                .modelName("qwen3.7-max-2026-05-20")
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
                .modelName("wan2.7-t2v-2026-06-12")
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


    @Bean
    public QdrantClient qdrantClient(){
        QdrantGrpcClient.Builder grpcClientBuilder = QdrantGrpcClient.newBuilder("120.48.1.247", 6334, false);
        return new QdrantClient(grpcClientBuilder.build());
    }

    /**
     * 基于 Qdrant的嵌入存储（适量数据库） Bean
     * @return
     */
    @Bean(name = "embeddingStore")
    public EmbeddingStore<TextSegment> embeddingStore(){
        return QdrantEmbeddingStore.builder()
                .host("120.48.1.247")
                .port(6334)
                .collectionName("test-qdrant")
                .build();
    }

    /**
     * 基于内存的 嵌入存储（矢量数据库） Bean
     * @return
     */
    @Bean(name = "inMemoryEmbeddingStore")
    public EmbeddingStore<TextSegment> inMemoryEmbeddingStore(){
        return new InMemoryEmbeddingStore<>();
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


    /**
     * 基于内存的缓存方案
     * @return
     */
    @Bean
    public ChatMemoryStore inMemoryChatMemoryStore(){
        return new InMemoryChatMemoryStore();
    }

    /**
     * InMemoryChatMemoryStore 本地存储介质的和 缓存方案 chatModel
     * 可以实现接口ChatMemoryStore， 自定义缓存介质，如 redis mysql等
     * @param chatModel
     * @return
     */
    @Bean(name = "localMemoryAssistant")
    public ChatMemoryAssistant localMemoryAssistant(@Qualifier("qwen") ChatModel chatModel, ChatMemoryStore inMemoryChatMemoryStore){

        ChatMemoryProvider  chatMemoryProvider = new ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object memoryId) {
                return MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(100)
                        .chatMemoryStore(inMemoryChatMemoryStore)
                        .build();
            }
        };

        return AiServices.builder(ChatMemoryAssistant.class)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .build();

    }


    /**
     * 缓存以token数 的缓存
     * @param inMemoryChatMemoryStore
     * @return
     */
    @Bean(name = "tokensMemory")
    public ChatMemoryProvider chatMemoryProvider(ChatMemoryStore inMemoryChatMemoryStore){
        return new ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object o) {
                return TokenWindowChatMemory.builder()
                        .id(o)
                        .maxTokens(1000,new OpenAiTokenCountEstimator("gpt-4."))
                        .chatMemoryStore(inMemoryChatMemoryStore)
                        .build();
            }
        };
    }

    /**
     * 基于message的缓存方案
     * @param inMemoryChatMemoryStore
     * @return
     */
    @Bean
    public ChatMemory messageMemory(ChatMemoryStore inMemoryChatMemoryStore){
       return MessageWindowChatMemory.builder()
                .maxMessages(50)
                .chatMemoryStore(inMemoryChatMemoryStore)
                .build();
    }



    /**
     * 使用 tool 工具的chat
     * @param chatModel  大模型
     * @param  tokensMemory
     * @return
     */
    @Bean(name = "functionAssistant")
    public FunctionAssistant functionAssistant(@Qualifier("qwen") ChatModel chatModel, @Qualifier("tokensMemory") ChatMemoryProvider tokensMemory){
        //工具说明 ToolSpecification
        ToolSpecification toolSpecification = ToolSpecification.builder()
                .name("xxx-开票助手")
                .description("根据用户提交的开票信息，开具发票")
                .parameters(JsonObjectSchema.builder()
                        .addStringProperty("companyName","公司名称")
                        .addStringProperty("dutyNumber","税号序列")
                        .addStringProperty("amount","开票金额，保留两位有效数字").build())
                .build();

        //业务逻辑 ToolEcecutor
        ToolExecutor toolExecutor = new ToolExecutor() {
            @Override
            public String execute(ToolExecutionRequest toolExecutionRequest, Object o) {
                System.out.println("开票工具 id："+ toolExecutionRequest.id());
                System.out.println("开票工具 名称："+toolExecutionRequest.name());
                String arguments = toolExecutionRequest.arguments();
                System.out.println("开票参数："+arguments);
                return "success";
            }
        };

        return AiServices.builder(FunctionAssistant.class)
                .chatModel(chatModel)
                .tools(Map.of(toolSpecification,toolExecutor))  //tools(Function Calling)
                .chatMemoryProvider(tokensMemory)
                .build();
    }



    @Bean
    public ContentRetriever contentRetriever(@Qualifier("inMemoryEmbeddingStore") EmbeddingStore<TextSegment> inMemoryEmbeddingStore){

        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(inMemoryEmbeddingStore)
                //.embeddingModel(embeddingModel)
                .maxResults(3) // 指定最多返回几条相关内容
                .minScore(0.75) // 设定相似度阈值，过滤掉不相关的内容
                .build();
    }




}
