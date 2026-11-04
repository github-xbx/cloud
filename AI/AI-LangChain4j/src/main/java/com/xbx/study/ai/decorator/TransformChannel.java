package com.xbx.study.ai.decorator;

import io.micrometer.observation.transport.ResponseContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.UnaryOperator;

public class TransformChannel extends TransportDecorator {

    private final UnaryOperator<String> transformer;

    public TransformChannel(Transport delegate, UnaryOperator<String> transformer) {
        super(delegate);
        this.transformer = transformer;
    }

    @Override
    public Mono<Void> sendUnary(String date, ResponseContext<Object> ctx) {

        return delegate.sendUnary(transformer.apply(date), ctx);

    }

    @Override
    public Mono<Void> sendStream(Flux<String> data, ResponseContext<Object> ctx) {
        return null;
    }
}
