package com.xbx.study.ai.controller;

import com.xbx.study.ai.decorator.DataPipeline;
import com.xbx.study.ai.decorator.EndpointHandler;
import com.xbx.study.ai.service.StreamChat;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("test")
public class Test {


    private final StreamChat streamChatService;

    public Test(StreamChat streamChatService){
        this.streamChatService = streamChatService;
    }


    @GetMapping("stream")
    public Flux<String> testStream(@RequestParam(value = "message",required = true) String message, ServerHttpResponse response){

        EndpointHandler handler = new EndpointHandler(streamChatService);

        //场景 1：HTTP SSE + 批量推送

        DataPipeline.Config cfg = new DataPipeline.Config(
        /* filterEmpty */     true,
        /* mask */            true,
        /* splitByNewline */  false,
        /* wrapJson */        true,
        /* batchSize */       20,
        /* idleTimeout */     Duration.ofSeconds(30),
        /* distinct */        false,
        /* gracefulError */   true
        );

       // handler.handle(new HttpChannel(response), cfg, true, message);

        return streamChatService.stream(message);
    }

}
