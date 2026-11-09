package com.network.reactor.netty.collect.client.message.impl;


import com.network.reactor.netty.collect.client.message.BasePaveMessage;
import com.network.reactor.netty.collect.client.message.PaveMessageOutput;
import io.netty.buffer.ByteBuf;

/**
 * 设置风速单位 请求，设置成功之后 客户端 -> 服务端的应答
 */
public class SetWindSpeedUnitRequest extends BasePaveMessage implements PaveMessageOutput {

    private int ack;

    public SetWindSpeedUnitRequest(int ack) {
        this.ack = ack;
    }

    public int getAck() {
        return ack;
    }

    public void setAck(int ack) {
        this.ack = ack;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeByte(ack);
    }
}
