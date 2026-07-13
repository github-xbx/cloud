package com.xbx.study.ai.controller;

import com.xbx.study.ai.service.ChatAssistant;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;


import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("chatModel")
public class ChatModelController {

    private static final Logger logger = LoggerFactory.getLogger(ChatModelController.class);

    @Resource(name = "deepseek")
    public ChatModel deepseek;


    public final ChatAssistant chatAssistant;

    public ChatModelController(ChatAssistant chatAssistant) {
        this.chatAssistant = chatAssistant;
    }


    @GetMapping("deepseek")
    public String deepseekCall(@RequestParam(name = "prompt", defaultValue = "介绍一下java?") String prompt){

        ChatResponse response = deepseek.chat(UserMessage.from(prompt));

        String text = response.aiMessage().text();

        logger.info("deepseek 回复 =>{}", text);
        TokenUsage tokenUsage = response.tokenUsage();
        logger.info("本次消耗的token => {}",tokenUsage);

        return text;

    }

    @GetMapping("deepseek1")
    public String deepseekCall1(@RequestParam(name = "prompt", defaultValue = "hello?") String prompt){

        String chat = chatAssistant.chat(prompt);

        logger.info("springboot 整合 deepseek  回复 =>{}", chat);
        return chat;

    }

    @Value("classpath:static/images/mi.jpg")
    private org.springframework.core.io.Resource resource;//import org.springframework.core.io.Resource;

    @Resource(name = "qwen")
    private ChatModel qwen;


    @GetMapping("qwen_image")
    public String qwenCallIImage() throws IOException {

        byte[] byteArray = resource.getContentAsByteArray();

        String base64Data = Base64.getEncoder().encodeToString(byteArray);

        UserMessage message = UserMessage.from(
                TextContent.from("从一下图片中获取来源网站名称，固件走势和5月30号股价"),
                ImageContent.from(base64Data, "image/jpg")
        );

        ChatResponse chat = qwen.chat(message);
        String text = chat.aiMessage().text();
        logger.info("deepseek images result => {}",text);
        return text;
    }



    @Resource(name = "qwenImageModel")
    private ImageModel qwenImageModel;

    @GetMapping("qwen_image_create")
    public String qwenCallIImageCreate() throws IOException {

        Response<Image> response = qwenImageModel.generate("美女");


        logger.info("qwenImageModel result => {}",response.content().url());

        return response.content().url().toString();

    }


}
