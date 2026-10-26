package com.xbx.study.ai.decorator.channel;

import com.xbx.study.ai.decorator.Transport;
import com.xbx.study.ai.decorator.TransportDecorator;
import io.micrometer.observation.transport.ResponseContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RetryChannel extends TransportDecorator {

    private final int maxAttempts;


    public RetryChannel(Transport transport,int maxAttempts) {
        super(transport);
        this.maxAttempts = maxAttempts;

    }

    @Override
    public Mono<Void> sendUnary(String date, ResponseContext<Object> ctx) {
        return delegate.sendUnary(date, ctx).retry(maxAttempts);
    }

    @Override
    public Mono<Void> sendStream(Flux<String> data, ResponseContext<Object> ctx) {
        // 关键：只重试"订阅即失败"的场景（连接失败、通道关闭）
        // 如果流已经发出去一部分再失败，重试会导致重复——由 operator 层的 onErrorResume 处理
        return delegate.sendStream(data, ctx).retry(maxAttempts);
    }
}
