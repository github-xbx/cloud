package com.xbx.study.ai.decorator;

import io.micrometer.observation.transport.ResponseContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 消息转换类
 */
public interface Transport {

    Mono<Void> sendUnary(String data, ResponseContext<Object> ctx);

    Mono<Void> sendStream(Flux<String> data, ResponseContext<Object> ctx);

}
