package com.network.reactor.netty.gateway.loadBalancer;

public interface ILoadBalancer<T> {

    T select();

}
