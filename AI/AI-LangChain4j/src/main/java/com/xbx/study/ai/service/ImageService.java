package com.xbx.study.ai.service;


import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.*;

@Service
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    private final ImageModel imageModel;



    public ImageService(
            @Qualifier("qwenImageModel") ImageModel imageModel) {
        this.imageModel = imageModel;

    }


    private ImageSynthesisParam imageParam(String prompt){
        //在创建 ImageSynthesis 客户端之前，设置全局的base URL
        Constants.baseHttpApiUrl = "https://ws-2gcnpdewhflb89dx.cn-beijing.maas.aliyuncs.com/api/v1";

        return ImageSynthesisParam.builder()
                .model("qwen-image-2.0-pro-2026-06-22")
                .apiKey(System.getenv("java_qwen_apikey"))
                .prompt(prompt)
                .n(1)
                .size("1920*1080")
                .build();

    }

    private void downloadImage(String imageUrl, String destinationFile) throws Exception {

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        Path targetPath = Paths.get(destinationFile);
        Files.createDirectories(targetPath.getParent());

        // 发送请求，将响应体直接写入文件
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(targetPath));



        if (response.statusCode() == 200) {
            System.out.println("图片已成功下载至: " + targetPath.toAbsolutePath());
        } else {
            System.err.println("下载失败，HTTP响应码: " + response.statusCode());
        }
    }


    /**
     * 生成图片 同步调用
     * @param prompt
     * @return
     */
    public Resource createOneImage(String prompt) throws Exception {


        // 1. 构建用户消息
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Collections.singletonList(Collections.singletonMap("text", prompt)))
                .build();

        // 2. 设置参数 (可选)
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("n", 1);                 // 生成图片数量，1-6张[reference:8]
        parameters.put("size", "2048*2048");    // 图片尺寸
        parameters.put("watermark", false);     // 是否添加水印
        parameters.put("prompt_extend", true);  // 是否自动扩写提示词[reference:9]

        // 3. 构建请求参数
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(System.getenv("java_qwen_apikey"))
                .model("qwen-image-2.0-pro-2026-06-22") // 使用你的模型名
                .messages(Collections.singletonList(userMessage))
                .parameters(parameters)
                .build();

        // 4. 创建客户端并同步调用
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalConversationResult result = conv.call(param); // 这里是同步调用！

        // 5. 解析结果，获取图片URL
        String imageUrl = result.getOutput()
                .getChoices()
                .get(0)
                .getMessage()
                .getContent()
                .get(0)
                .get("image").toString(); // 注意：是 "image" 字段[reference:10][reference:11]
        System.out.println("生成的图片URL: " + imageUrl);

        String filePath = "E:\\file\\"+UUID.randomUUID().toString()+".jpg";
        //保存到本地
        downloadImage(imageUrl,filePath);


        byte[] bytes = Files.readAllBytes(Path.of(filePath));

        return new ByteArrayResource(bytes);
    }




}
