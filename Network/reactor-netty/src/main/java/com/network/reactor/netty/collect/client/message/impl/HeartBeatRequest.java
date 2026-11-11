package com.network.reactor.netty.collect.client.message.impl;


import com.network.reactor.netty.collect.client.message.BasePaveMessage;
import com.network.reactor.netty.collect.client.message.PaveMessageOutput;
import io.netty.buffer.ByteBuf;

/**
 * 心跳请求 Message 每次都发送为处理的记录数
 */
public class HeartBeatRequest extends BasePaveMessage implements PaveMessageOutput {


    /**
     * 未确认记录数
     */
    private final long noConfirm = 0;


    public HeartBeatRequest() {
        super();
        setFrame(1);
    }

    /**
     *  编码 心跳请求
     */
    @Override
    public void encode(ByteBuf buf) {
        buf.writeInt((int) noConfirm);

    }

    @Override
    public String toString() {
        return "HeartBeatRequest{" +
                "noConfirm=" + noConfirm +
                ", BasePaveMessage{frame=" + frame +
                ", sequence=" + sequence +
                ", crcValid=" + crcValid +
                "} }";
    }
}
