package study;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class MonoAndFluxDemo {


    /**
     * Mono 表示 0个或1个数据
     * 处理一个一部结果
     * Mono.just 是冷 Publisher，订阅时才向订阅者发出数据；不过值本身是在创建时就准备好的。
     */
    @Test
    public void MonoDemo(){
        Mono<String> mono = Mono.just("你好 Mono");
        mono.subscribe(m -> {
            System.out.println(m);
        });

        Mono<String> defer = Mono.defer(() -> {
            System.out.println("执行mono逻辑");
            return Mono.just("hello");
        });

        defer.subscribe(System.out::println);
        defer.subscribe(System.out::println);

        mono.subscribe(System.out::println);
    }


    @Test
    public void FluxDemo(){
        Flux<Integer> range = Flux.range(1, 5);


        range.subscribe(
                v -> System.out.println("value=>"+v),
                e -> System.out.println("errror=>"+e),
                () -> System.out.println("flux 数据流结束")
        );

    }


    /**
     *
     * publishOn 前面的操作符通常在原来的执行线程运行。
     * publishOn 后面的操作符切换到指定 Scheduler。
     *
     */
    @Test
    public void publishOnDemo(){

        Flux.range(1,5)
                .map(value -> {
                    System.out.println("map1：" + Thread.currentThread().getName());
                    return value;
                })
                .publishOn(Schedulers.parallel())
                .map(value -> {
                    System.out.println("map1：" + Thread.currentThread().getName());
                    return value;
                })
                .publishOn(Schedulers.parallel())
                .map(value -> {
                    System.out.println("map1：" + Thread.currentThread().getName());
                    return value;
                })
                .publishOn(Schedulers.parallel())
                .map(value -> {
                    System.out.println("map1：" + Thread.currentThread().getName());
                    return value;
                })
                .subscribe();

    }




}
