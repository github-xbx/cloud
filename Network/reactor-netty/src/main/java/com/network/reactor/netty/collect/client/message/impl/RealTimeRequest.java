package com.network.reactor.netty.collect.client.message.impl;


import com.network.reactor.netty.collect.client.message.BasePaveMessage;
import com.network.reactor.netty.collect.client.message.PaveMessageOutput;
import io.netty.buffer.ByteBuf;

public class RealTimeRequest extends BasePaveMessage implements PaveMessageOutput {

    private final Integer type = 0;


    @Override
    public void encode(ByteBuf buf) {
        buf.writeByte(type);
    }
}
