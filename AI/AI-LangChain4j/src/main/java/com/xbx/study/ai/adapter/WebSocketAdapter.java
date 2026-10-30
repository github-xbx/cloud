package com.xbx.study.ai.adapter;

import com.xbx.study.ai.service.BaseAiChat;
import reactor.core.publisher.Flux;

public class WebSocketAdapter extends Target{
    public WebSocketAdapter(BaseAiChat baseAiChat) {
        super(baseAiChat);
    }


    @Override
    public Flux<String> stream(String prompt) {
        return super.stream(prompt);
    }

    @Override
    public String chat(String prompt) {
        return super.chat(prompt);
    }

}
