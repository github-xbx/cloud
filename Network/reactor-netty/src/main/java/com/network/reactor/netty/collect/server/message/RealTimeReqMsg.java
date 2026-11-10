package com.network.reactor.netty.collect.server.message;

import io.netty.buffer.ByteBuf;



public class RealTimeReqMsg extends BasePaveMsg implements PaveMsgOutput {

    private Integer type = 0;

    public RealTimeReqMsg() {
    }

    public RealTimeReqMsg(Integer sequence) {
        this.sequence = sequence;
        this.frame = 4;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeByte(this.type);
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
