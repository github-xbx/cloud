package com.xbx.study.ai.decorator;


/**
 * 装饰器基类
 */
public abstract class TransportDecorator implements Transport {

    protected final Transport delegate;
    public TransportDecorator(Transport delegate) {
        this.delegate = delegate;
    }

}
