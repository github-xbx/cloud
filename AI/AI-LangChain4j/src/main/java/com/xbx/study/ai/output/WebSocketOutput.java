package com.xbx.study.ai.output;


import reactor.core.publisher.Flux;

import java.util.Map;

public class WebSocketOutput extends AiMessageOutput {


    public WebSocketOutput(AiMessageService service) {
        super(service);
    }

    @Override
    public Flux<String> streamOutput(Map<String, String> param) {
        return null;
    }

    @Override
    public String output(Map<String, String> param) {
        return "";
    }
}
