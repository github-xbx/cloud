package com.xbx.study.netty.collect_client.message.impl;

import com.xbx.study.netty.collect_client.message.BasePaveMessage;
import com.xbx.study.netty.collect_client.message.PaveMessageOutput;
import io.netty.buffer.ByteBuf;

public class RealTimeRequest extends BasePaveMessage implements PaveMessageOutput {

    private final Integer type = 0;


    @Override
    public void encode(ByteBuf buf) {
        buf.writeByte(type);
    }
}
