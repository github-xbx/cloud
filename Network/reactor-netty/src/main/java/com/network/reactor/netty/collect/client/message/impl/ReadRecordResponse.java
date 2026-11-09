package com.network.reactor.netty.collect.client.message.impl;


import com.network.reactor.netty.collect.client.message.BasePaveMessage;
import com.network.reactor.netty.collect.client.message.PaveMessageInput;
import io.netty.buffer.ByteBuf;

public class ReadRecordResponse extends BasePaveMessage implements PaveMessageInput {

    private Integer confirm;

    private Integer count;

    public Integer getConfirm() {
        return confirm;
    }

    public void setConfirm(Integer confirm) {
        this.confirm = confirm;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public void decode(ByteBuf buf) {
        this.confirm = buf.readUnsignedShort();
        this.count = buf.readUnsignedShort();
    }
}
