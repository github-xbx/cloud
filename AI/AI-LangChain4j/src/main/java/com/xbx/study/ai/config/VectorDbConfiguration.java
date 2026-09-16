package com.xbx.study.ai.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaApiVersion;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class VectorDbConfiguration {


    /**
     * 基于 Qdrant的嵌入存储（适量数据库） Bean
     * @return
     */
    @Bean(name = "embeddingStore")
    @ConditionalOnClass({
            QdrantEmbeddingStore.class,
    })
    public EmbeddingStore<TextSegment> embeddingStore(){
        System.out.println("111");
        return QdrantEmbeddingStore.builder()
                .host("120.48.1.247")
                .port(6334)
                .collectionName("test-qdrant")
                .build();
    }


    /**
     * 基于 ChromaDB的嵌入存储（矢量数据库） Bean
     * @return
     */
    @Bean(name = "chromaEmbeddingStore")
    @ConditionalOnClass({
            ChromaEmbeddingStore.class,
    })
    public EmbeddingStore<TextSegment> chromaEmbeddingStore(){
        System.out.println("222");
        return ChromaEmbeddingStore.builder()
                .baseUrl("http://127.0.0.1:8000")
                .collectionName("langchain4j_test")
                .apiVersion(ChromaApiVersion.V2)
                .timeout(Duration.ofSeconds(5)) //超时时间5s
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

}
