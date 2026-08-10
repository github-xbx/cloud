package com.xbx.study.dubbo.service;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 业务委托 Bean —— 普通 Spring Bean，Spring AOP 完全生效
 * <p>
 * 所有 @RateLimiter、@CircuitBreaker、@Retry 等 Spring AOP 注解
 * 都可以正常使用，不受 Dubbo 代理影响。
 */
@Component
public class DubboDemoDelegate {

    private static final Logger log = LoggerFactory.getLogger(DubboDemoDelegate.class);

    /**
     * 核心业务逻辑 + 限流
     * <p>
     * {@code @RateLimiter(name = "helloLimiter")} 对应 application.yml 中
     * {@code resilience4j.ratelimiter.instances.helloLimiter} 的配置。
     */
    @RateLimiter(name = "dubboRateLimiter", fallbackMethod = "helloFallback")
    @CircuitBreaker(name = "dubboCircuitBreaker", fallbackMethod = "circuitBreakerFallback")
    public String hello(String name) {
        System.out.println("rpc => "+name);
        int i = 1/0;
        // 纯业务逻辑，不需要任何限流代码
        return "hello word =>" + name;
    }


    /**
     * 限流触发时的降级方法
     * <p>
     * 方法签名要求：与原方法参数一致 + 额外接收 {@link RequestNotPermitted}（可选）或 {@link Throwable}。
     */
    @SuppressWarnings("unused")
    private String helloFallback(String name, RequestNotPermitted e) {
        log.warn("hello 接口被限流，request not permitted");
        return "限流了";
    }

    private String circuitBreakerFallback(String name, Throwable throwable){
        if (throwable instanceof CallNotPermittedException){
            return name + "熔断了。。。";
        }
        return throwable.toString();
    }
}
