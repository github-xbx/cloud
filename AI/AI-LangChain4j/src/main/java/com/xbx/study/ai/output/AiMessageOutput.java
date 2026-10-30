package com.xbx.study.ai.output;

import com.xbx.study.ai.service.BaseAiChat;
import reactor.core.publisher.Flux;

import java.util.Map;

public abstract class AiMessageOutput {

    protected AiMessageService aiMessageService;

    public AiMessageOutput(AiMessageService service){
        this.aiMessageService = service;
    }


    public abstract Flux<String> streamOutput(Map<String,String> param);


    public abstract String output(Map<String,String> param);
}
