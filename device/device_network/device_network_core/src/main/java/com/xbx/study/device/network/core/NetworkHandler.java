package com.xbx.study.device.network.core;

/**
 * network 处理器
 * @param <M>
 */
public interface NetworkHandler<M> {
    /**
     * 处理入站消息
     * @param message
     */
    void onMessage(M message);

}
