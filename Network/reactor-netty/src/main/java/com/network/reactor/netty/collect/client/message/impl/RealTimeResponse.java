package com.network.reactor.netty.collect.client.message.impl;


import com.network.reactor.netty.collect.client.domain.PaveRecord;
import com.network.reactor.netty.collect.client.message.BasePaveMessage;
import com.network.reactor.netty.collect.client.message.PaveMessageInput;
import io.netty.buffer.ByteBuf;

public class RealTimeResponse extends BasePaveMessage implements PaveMessageInput {

    /**
     * 未确认记录数
     */
    private Long noConfirm;

    /**
     * 记录
     */
    private PaveRecord paveRecord;


    public Long getNoConfirm() {
        return noConfirm;
    }

    public void setNoConfirm(Long noConfirm) {
        this.noConfirm = noConfirm;
    }

    public PaveRecord getPaveRecord() {
        return paveRecord;
    }

    public void setPaveRecord(PaveRecord paveRecord) {
        this.paveRecord = paveRecord;
    }

    @Override
    public void decode(ByteBuf buf) {
        this.noConfirm = buf.readUnsignedInt();
        this.paveRecord = parseData(buf);
    }
}
