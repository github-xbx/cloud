package com.xbx.study.ai.decorator.output;

import com.xbx.study.ai.decorator.Transport;
import io.micrometer.observation.transport.ResponseContext;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.nio.charset.StandardCharsets;

public class HttpOutput implements Transport {


    private final ServerHttpResponse response;
    public HttpOutput(ServerHttpResponse  response) {
        this.response = response;
    }


    @Override
    public Mono<Void> sendUnary(String data, ResponseContext<Object> ctx) {
        return response.writeWith(Mono.just(
                response.bufferFactory().wrap(data.getBytes(StandardCharsets.UTF_8))));
    }

    @Override
    public Mono<Void> sendStream(Flux<String> data, ResponseContext<Object> ctx) {
        response.getHeaders().setContentType(MediaType.TEXT_EVENT_STREAM);
        Flux<DataBuffer> body =  data.map(s -> response.bufferFactory().wrap(("data:"+s+"\n\b").getBytes(StandardCharsets.UTF_8)));
        return response.writeWith(body);
    }
}
