package com.xbx.study.netty.collect_client.message.impl;

import com.xbx.study.netty.collect_client.message.BasePaveMessage;
import com.xbx.study.netty.collect_client.message.PaveMessageInput;
import io.netty.buffer.ByteBuf;

import java.util.Objects;

/**
 * 设置风速单位响应 对应服务端的请求
 */
public class SetWindSpeedUnitResponse extends BasePaveMessage implements PaveMessageInput {

    // 风速显示单位 0：km/h； 1：m/s
    private int unit;

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    @Override
    public void decode(ByteBuf buf) {
        this.unit = buf.readUnsignedByte();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SetWindSpeedUnitResponse that = (SetWindSpeedUnitResponse) o;
        return unit == that.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(unit);
    }

    @Override
    public String toString() {
        return "SetWindSpeedUnitResponse{" +
                "unit=" + unit +
                ", frame=" + frame +
                ", sequence=" + sequence +
                ", crcValid=" + crcValid +
                '}';
    }
}
