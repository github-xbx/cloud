package com.network.reactor.netty.collect.server.message;

import io.netty.buffer.ByteBuf;

import java.util.Objects;


public class DeviceInfoReqMsg extends BasePaveMsg implements PaveMsgOutput {

    private Integer type = 0;

    public DeviceInfoReqMsg() {
        this.frame = 2;
        this.sequence = 0;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeByte(type);
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DeviceInfoReqMsg that = (DeviceInfoReqMsg) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type);
    }
}
