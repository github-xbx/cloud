package com.network.reactor.netty.collect.server.message;

import io.netty.buffer.ByteBuf;

import java.util.Objects;


public class HeartBeatReqMsg extends BasePaveMsg implements PaveMsgInput {

    private Long noConfirm;

    @Override
    public void decode(ByteBuf buf) {
        this.noConfirm = (long) buf.readInt();
    }

    public Long getNoConfirm() {
        return noConfirm;
    }

    public void setNoConfirm(Long noConfirm) {
        this.noConfirm = noConfirm;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        HeartBeatReqMsg that = (HeartBeatReqMsg) o;
        return Objects.equals(noConfirm, that.noConfirm);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(noConfirm);
    }
}
