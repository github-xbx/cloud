package com.xbx.study.ai.decorator.output;

import com.xbx.study.ai.decorator.Transport;
import io.micrometer.observation.transport.ResponseContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class WebSocketChannel implements Transport {
    private final WebSocketSession session;
    public WebSocketChannel(WebSocketSession session) { this.session = session; }


    @Override
    public Mono<Void> sendUnary(String date, ResponseContext<Object> ctx) {
        return session.send(Mono.just(session.textMessage(data)));
    }

    @Override
    public Mono<Void> sendStream(Flux<String> data, ResponseContext<Object> ctx) {
        return session.send(data.map(session::textMessage));
    }
}
