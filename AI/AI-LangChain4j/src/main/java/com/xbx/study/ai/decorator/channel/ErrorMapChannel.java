package com.xbx.study.ai.decorator.channel;

import com.xbx.study.ai.decorator.Transport;
import com.xbx.study.ai.decorator.TransportDecorator;
import io.micrometer.observation.transport.ResponseContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class ErrorMapChannel extends TransportDecorator {

    private final Function<Throwable, String> mapper;

    public ErrorMapChannel(Transport delegate, Function<Throwable, String> mapper) {
        super(delegate);
        this.mapper = mapper;

    }


    @Override
    public Mono<Void> sendUnary(String date, ResponseContext<Object> ctx) {
        return delegate.sendUnary(date, ctx).onErrorResume(ex -> delegate.sendUnary(mapper.apply(ex), ctx));
    }

    @Override
    public Mono<Void> sendStream(Flux<String> data, ResponseContext<Object> ctx) {
        // 发送阶段的错误，转为向通道补发一条错误消息后正常结束
        return delegate.sendStream(data, ctx).onErrorResume(ex -> delegate.sendStream(Flux.just(mapper.apply(ex)), ctx));
    }
}
