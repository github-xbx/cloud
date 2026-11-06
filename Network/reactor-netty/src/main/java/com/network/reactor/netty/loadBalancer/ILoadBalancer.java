package com.network.reactor.netty.loadBalancer;

public interface ILoadBalancer<T> {

    T select();

}
