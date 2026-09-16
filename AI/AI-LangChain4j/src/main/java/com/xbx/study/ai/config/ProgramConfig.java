package com.xbx.study.ai.config;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ProgramConfig {


    @Bean
    public RestClient restClient(){
        return RestClient.builder().build();
    }

    @Bean
    public QdrantClient qdrantClient(){
        QdrantGrpcClient.Builder grpcClientBuilder = QdrantGrpcClient.newBuilder("120.48.1.247", 6334, false);
        return new QdrantClient(grpcClientBuilder.build());
    }

}
