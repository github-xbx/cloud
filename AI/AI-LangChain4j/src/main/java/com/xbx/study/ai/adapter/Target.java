package com.xbx.study.ai.adapter;

import com.xbx.study.ai.service.BaseAiChat;
import reactor.core.publisher.Flux;

public abstract class Target {


    private final BaseAiChat baseAiChat;

    protected Target(BaseAiChat baseAiChat) {
        this.baseAiChat = baseAiChat;
    }


    public Flux<String> stream(String prompt){
        return baseAiChat.stream(prompt);
    }

    public String chat(String prompt){
        return baseAiChat.chat(prompt);
    }

}
