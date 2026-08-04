package com.xbx.study.ai.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(wiringMode = EXPLICIT,chatModel = "qwen")
public interface ChatAssistant {


    String chat(String prompt);


    @SystemMessage("输出限制: 只输出城市名称")
    @UserMessage("根据地址： {{address}}， 输出地址所在的城市名称")
    String addressToCity(@V("address")String address);

}
