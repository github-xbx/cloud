package com.xbx.study.test.proxy.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * 自定义 jdk 运行时生成的代理类
 * 正常情况下 这个类时再程序运行时生成，这里主要时为了学习一下动态代里所以说自己试着模拟一个
 */
public class $MyProxyRuntime extends Proxy implements Test {
    /**
     * Constructs a new {@code Proxy} instance from a subclass
     * (typically, a dynamic proxy class) with the specified value
     * for its invocation handler.
     *
     * @param h the invocation handler for this proxy instance
     * @throws NullPointerException if the given invocation handler, {@code h},
     *                              is {@code null}.
     */
    protected $MyProxyRuntime(InvocationHandler h) {
        super(h);
    }

    @Override
    public void sout() {

    }
}
