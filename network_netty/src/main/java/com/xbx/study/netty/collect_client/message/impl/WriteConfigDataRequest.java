package com.xbx.study.netty.collect_client.message.impl;

import com.xbx.study.netty.collect_client.message.BasePaveMessage;
import com.xbx.study.netty.collect_client.message.PaveMessageOutput;
import io.netty.buffer.ByteBuf;

public class WriteConfigDataRequest extends BasePaveMessage implements PaveMessageOutput {

    private int ack;

    public int getAck() {
        return ack;
    }

    public void setAck(int ack) {
        this.ack = ack;
    }

    public WriteConfigDataRequest(int ack) {
        this.frame = 16;
        this.ack = ack;
    }

    @Override
    public void encode(ByteBuf buf) {

        buf.writeByte(ack);

    }
}
