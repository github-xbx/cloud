package com.network.reactor.netty.collect.server.handler.reactive.handler;

import reactor.core.publisher.Mono;


/**
 * 横向扩展点：新增一种消息 = 新增一个实现类 + 在 MessageRouter 上注册一行。
 *
 * @param <T> 该 handler 负责的消息类型
 */
public interface MessageHandler<T> {

    /** 负责的消息类型，作为注册表的 key */
    Class<T> type();

    /**
     * 处理一条消息，返回「下一步要下发的消息」。
     * 只做业务和协议应答，不负责改状态——状态迁移在路由表里声明。
     */
    Mono<Object> handle(T msg);
}
