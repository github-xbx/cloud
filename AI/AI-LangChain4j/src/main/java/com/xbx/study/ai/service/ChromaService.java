package com.xbx.study.ai.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class ChromaService {

    private static final Logger logger = LoggerFactory.getLogger(ChromaService.class);

    private final EmbeddingStore<TextSegment> chromaEmbeddingStore;
    private final EmbeddingModel qwenEmbedding;

    public ChromaService(
            @Qualifier("chromaEmbeddingStore") EmbeddingStore<TextSegment> chromaEmbeddingStore,
            @Qualifier("qwen_embedding") EmbeddingModel qwenEmbedding) {
        this.chromaEmbeddingStore = chromaEmbeddingStore;
        this.qwenEmbedding = qwenEmbedding;
    }


    public void add(String text){
        //设置 元数据 保存向量id
        TextSegment textSegment = TextSegment.from(text, Metadata.from("customId", UUID.randomUUID().toString()));
        //通过向量模型生成向量
        Embedding textEmbedding = qwenEmbedding.embed(textSegment).content();

        String id = chromaEmbeddingStore.add(textEmbedding, textSegment);

        logger.debug("保存的id => {}",id);
    }


    public void addFile(MultipartFile multipartFile){

        try(InputStream inputStream = multipartFile.getInputStream()) {

            // 创建文档解析器（这里以 Apache Tika 为例） Apache Tika 能自动检测并解析几乎所有格式的文件
            ApacheTikaDocumentParser documentParser = new ApacheTikaDocumentParser();
            Document parse = documentParser.parse(inputStream);

            // 对这个 Document 进行 分割 (Split)、向量化 (Embed) 并存入数据库 (Store)
            DocumentSplitter recursive = DocumentSplitters.recursive(500, 50);
            EmbeddingStoreIngestor embeddingStoreIngestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(recursive)
                    .embeddingModel(qwenEmbedding)
                    .embeddingStore(chromaEmbeddingStore)
                    //消息转换器 之后，你可以从数据库查询时返回的 TextSegment 中，通过元数据 "customId" 获取这个 ID。
                    .textSegmentTransformer(textSegment -> {
                        String uuid = multipartFile.getName() + "_" + UUID.randomUUID().toString();
                        // 关键：将 ID 作为元数据存入
                        textSegment.metadata().put("customId", uuid);
                        return textSegment;
                    })
                    .build();
            embeddingStoreIngestor.ingest(parse);

        } catch (Exception e) {
            throw new RuntimeException("保存文件的向量数据失败 Exception => "+e);
        }
        logger.debug("保存文件向量数据成功.");
    }

    public void addFile(String filePath){

        String fileName = filePath.substring(filePath.lastIndexOf("."), filePath.lastIndexOf("."));

        //加载单个文档
        Document document = FileSystemDocumentLoader.loadDocument(filePath);

        // 对这个 Document 进行 分割 (Split)、向量化 (Embed) 并存入数据库 (Store)
        DocumentSplitter recursive = DocumentSplitters.recursive(1000, 200);
        EmbeddingStoreIngestor embeddingStoreIngestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(recursive)
                .embeddingModel(qwenEmbedding)
                .embeddingStore(chromaEmbeddingStore)
                //消息转换器 之后，你可以从数据库查询时返回的 TextSegment 中，通过元数据 "customId" 获取这个 ID。
                .textSegmentTransformer(textSegment -> {
                    String uuid = fileName + "_" + UUID.randomUUID().toString();
                    // 关键：将 ID 作为元数据存入
                    textSegment.metadata().put("customId", uuid);
                    return textSegment;
                })
                .build();
        embeddingStoreIngestor.ingest(document);


    }


    public String query(String text){

        Embedding queryTextEmbedding = qwenEmbedding.embed(text).content();

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryTextEmbedding)
                .maxResults(1) //返回1条最相似的结果
                .minScore(0.8)  //最低相似度阈值
                .build();

        EmbeddingSearchResult<TextSegment> searchResult = chromaEmbeddingStore.search(searchRequest);

        //返回查询 到的文本
        return searchResult.matches().getFirst().embedded().text();

    }



}
