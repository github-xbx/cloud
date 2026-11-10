package com.network.reactor.netty.collect.server.message;

import io.netty.buffer.ByteBuf;

import java.util.Objects;


public class ReadRecordReqMsg extends BasePaveMsg implements PaveMsgOutput {

    private Integer confirm = 0;

    private Integer count = 0;

    public ReadRecordReqMsg() {
    }

    public ReadRecordReqMsg(Integer sequence, Integer confirm, Integer count) {
        this.sequence = sequence;
        this.frame = 3;
        this.confirm = confirm;
        this.count = count;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeShort(confirm);
        buf.writeShort(count);
    }

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
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReadRecordReqMsg that = (ReadRecordReqMsg) o;
        return Objects.equals(confirm, that.confirm) && Objects.equals(count, that.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(confirm, count);
    }
}
