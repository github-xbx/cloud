package com.xbx.study.netty.service;

import com.google.protobuf.Message;
import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultPromise;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 请求服务接口，基于 Netty Channel 和 Protobuf 提供同步/异步的请求-响应模型。
 * <p>
 * 通过 {@link DefaultPromise} 实现同步等待机制。请求发送时通过 {@link #getReqKey(Channel, Message)}
 * 生成缓存键，收到响应时通过 {@link #getRespKey(Channel, Message)} 生成缓存键，两者匹配后唤醒等待线程。
 * </p>
 * <p>
 * 所有请求和响应统一使用 Protobuf 的 {@link Message} 作为载体，业务差异由具体的 Handler 层处理，
 * 本接口只负责消息的发送和 Promise 的管理。
 * </p>
 * <p>
 * 实现类需实现：
 * <ul>
 *   <li>{@link #getReqKey(Channel, Message)} - 从请求消息中提取缓存键</li>
 *   <li>{@link #getRespKey(Channel, Message)} - 从响应消息中提取缓存键</li>
 * </ul>
 * 请求键和响应键必须一致，才能正确匹配 Promise。
 * </p>
 */
public interface RequestService {

    /** 默认同步请求超时时间（秒） */
    static final int DEFAULT_TIMEOUT = 30;

    /** 序列号生成器，实现类可用于生成请求ID */
    AtomicInteger SEQ = new AtomicInteger(1);

    /**
     * Promise 缓存，所有实现共享。
     * <p>Key 由 {@link #getReqKey(Channel, Message)} / {@link #getRespKey(Channel, Message)} 生成，
     * Value 为对应的 Promise 对象。</p>
     */
    Map<String, DefaultPromise<Message>> PROMISE_CACHE = new ConcurrentHashMap<>();

    /**
     * 发送异步请求（fire-and-forget），不等待响应。
     *
     * @param channel 目标 Channel
     * @param req     Protobuf 请求消息
     */
    default void asyncRequest(Channel channel, Message req) {
        channel.writeAndFlush(req);
    }

    /**
     * 发送同步请求，使用默认超时时间 {@link #DEFAULT_TIMEOUT} 秒。
     *
     * @param channel 目标 Channel
     * @param req     Protobuf 请求消息
     * @return Protobuf 响应消息
     * @throws Exception 超时或中断时抛出异常
     */
    default Message syncRequest(Channel channel, Message req) throws Exception {
        return syncRequest(channel, req, DEFAULT_TIMEOUT);
    }

    /**
     * 发送同步请求，阻塞当前线程直到收到响应或超时。
     * <p>
     * 流程：
     * <ol>
     *   <li>通过 {@link #getReqKey(Channel, Message)} 生成缓存键</li>
     *   <li>创建 Promise 并存入 {@link #PROMISE_CACHE}</li>
     *   <li>通过 Channel 发送请求</li>
     *   <li>阻塞等待 {@link #setResult(Channel, Message)} 唤醒</li>
     *   <li>返回响应结果，finally 中清理缓存</li>
     * </ol>
     * </p>
     *
     * @param channel 目标 Channel
     * @param req     Protobuf 请求消息
     * @param timeout 超时时间（秒）
     * @return Protobuf 响应消息
     * @throws Exception 超时或中断时抛出异常
     */
    default Message syncRequest(Channel channel, Message req, long timeout) throws Exception {
        String key = getReqKey(channel, req);
        try {
            DefaultPromise<Message> promise = new DefaultPromise<>(channel.eventLoop());
            PROMISE_CACHE.put(key, promise);
            channel.writeAndFlush(req);
            return promise.get(timeout, TimeUnit.SECONDS);
        } finally {
            PROMISE_CACHE.remove(key);
        }
    }

    /**
     * 设置响应结果，唤醒等待中的同步请求。
     * <p>
     * 通常在 Channel 的入站处理器（Handler）中收到响应时调用。
     * 通过 {@link #getRespKey(Channel, Message)} 生成缓存键，与发送时的请求键匹配。
     * </p>
     *
     * @param channel 响应来源的 Channel
     * @param resp    Protobuf 响应消息
     * @return true-设置成功（存在等待中的 Promise），false-无匹配的 Promise
     */
    default boolean setResult(Channel channel, Message resp) {
        String key = getRespKey(channel, resp);
        DefaultPromise<Message> promise = PROMISE_CACHE.get(key);
        if (promise == null) return false;
        promise.setSuccess(resp);
        return true;
    }

    /**
     * 从请求消息中提取缓存键。
     * <p>
     * 该键用于在 {@link #PROMISE_CACHE} 中存储 Promise，
     * 必须与 {@link #getRespKey(Channel, Message)} 返回的键一致才能正确匹配。
     * </p>
     * <p>
     * 常见策略：
     * <ul>
     *   <li>基于请求中的 requestId 字段（推荐，支持同一 Channel 并发多个请求）</li>
     *   <li>基于 Channel ID（同一 Channel 同时只能有一个进行中的请求）</li>
     * </ul>
     * </p>
     *
     * @param channel 当前 Channel
     * @param req     Protobuf 请求消息
     * @return 缓存键
     */
    String getReqKey(Channel channel, Message req);

    /**
     * 从响应消息中提取缓存键。
     * <p>
     * 该键用于在 {@link #PROMISE_CACHE} 中查找对应的 Promise，
     * 必须与 {@link #getReqKey(Channel, Message)} 返回的键一致才能正确匹配。
     * </p>
     *
     * @param channel 当前 Channel
     * @param resp    Protobuf 响应消息
     * @return 缓存键
     */
    String getRespKey(Channel channel, Message resp);
}