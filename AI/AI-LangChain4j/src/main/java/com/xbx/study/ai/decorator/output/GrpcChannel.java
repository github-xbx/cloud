package com.xbx.study.ai.decorator.output;

public class GrpcChannel<T> implements OutputChannel {
    private final StreamObserver<T> observer;
    private final Function<String, T> toProto;
    public GrpcChannel(StreamObserver<T> observer, Function<String, T> toProto) {
        this.observer = observer;
        this.toProto = toProto;
    }

    @Override
    public Mono<Void> unary(String data) {
        return Mono.fromRunnable(() -> {
            observer.onNext(toProto.apply(data));
            observer.onCompleted();
        });
    }

    @Override
    public Mono<Void> stream(Flux<String> data) {
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
