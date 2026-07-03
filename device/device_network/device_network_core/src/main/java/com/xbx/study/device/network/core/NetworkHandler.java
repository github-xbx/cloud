package com.xbx.study.device.network.core;

import com.xbx.study.device.network.core.message.BaseMessage;

/**
 * network 处理器
 * @param <M>
 */
public interface NetworkHandler<M extends BaseMessage> {
    /**
     * 处理入站消息
     * @param message 消息内容
     */
    void onMessage(M message);

    /**
     * 处理入站消息（带会话ID）
     * @param sessionId 会话ID
     * @param message 消息内容
     */
    default void onMessage(String sessionId, M message) {
        // 默认实现：调用单参数版本
        onMessage(message);
    }
}
