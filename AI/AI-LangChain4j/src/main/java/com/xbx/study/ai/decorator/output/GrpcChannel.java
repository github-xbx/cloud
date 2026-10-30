package com.xbx.study.ai.decorator.output;

import com.xbx.study.ai.decorator.Transport;
import io.grpc.stub.StreamObserver;
import io.micrometer.observation.transport.ResponseContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class GrpcChannel<T> implements Transport {
    private final StreamObserver<T> observer;
    private final Function<String, T> toProto;
    public GrpcChannel(StreamObserver<T> observer, Function<String, T> toProto) {
        this.observer = observer;
        this.toProto = toProto;
    }



    @Override
    public Mono<Void> sendUnary(String data, ResponseContext<Object> ctx) {
        return Mono.fromRunnable(() -> {
            observer.onNext(toProto.apply(data));
            observer.onCompleted();
        });
    }

    @Override
    public Mono<Void> sendStream(Flux<String> data, ResponseContext<Object> ctx) {
        // 用 Mono.create 把 StreamObserver 的完成/失败反向传播回 Mono
        return Mono.create(sink ->
                data.subscribe(
                        s -> observer.onNext(toProto.apply(s)),
                        ex -> { observer.onError(ex); sink.error(ex); },
                        () -> { observer.onCompleted(); sink.success(); }
                )
        );
    }
}
