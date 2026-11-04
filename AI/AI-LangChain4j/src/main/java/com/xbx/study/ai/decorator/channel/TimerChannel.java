package com.xbx.study.ai.decorator.channel;

import com.xbx.study.ai.decorator.Transport;
import com.xbx.study.ai.decorator.TransportDecorator;
import io.micrometer.observation.transport.ResponseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 耗时装饰器
 */
public class TimerChannel extends TransportDecorator {
    private static final Logger log = LoggerFactory.getLogger(TimerChannel.class);

    public TimerChannel(Transport delegate) {
        super(delegate);
    }

    @Override
    public Mono<Void> sendUnary(String date, ResponseContext<Object> ctx) {

        return time(delegate.sendUnary(date, ctx), "unary");
    }

    @Override
    public Mono<Void> sendStream(Flux<String> data, ResponseContext<Object> ctx) {
        return time(delegate.sendStream(data, ctx), "stream");
    }


    private Mono<Void> time(Mono<Void> mono, String kind){
        long time = System.nanoTime();
        return mono.doFinally(sig ->
                log.info("[{}] done in {}ms, signal={}",kind, (System.nanoTime() - time) / 1_000_000, sig));
    }
}
