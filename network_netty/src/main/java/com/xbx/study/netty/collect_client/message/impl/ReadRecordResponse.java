package com.xbx.study.netty.collect_client.message.impl;

import com.xbx.study.netty.collect_client.message.BasePaveMessage;
import com.xbx.study.netty.collect_client.message.PaveMessageInput;
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
