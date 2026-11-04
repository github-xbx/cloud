package com.xbx.study.ai.output;


import reactor.core.publisher.Flux;

import java.util.Map;

public class GrpcOutPut extends AiMessageOutput {


    public GrpcOutPut(AiMessageService service) {
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
