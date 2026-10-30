package com.xbx.study.ai.output;


import reactor.core.publisher.Flux;

import java.util.Map;

public class HttpOutput extends AiMessageOutput {


    public HttpOutput(AiMessageService service) {
        super(service);
    }

    @Override
    public Flux<String> streamOutput(Map<String, String> param) {
        return this.aiMessageService.streamService().stream(param.get("message"));
    }

    @Override
    public String output(Map<String, String> param) {
        return "";
    }
}
