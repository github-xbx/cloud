package com.network.reactor.netty.collect.server.message;

import io.netty.buffer.ByteBuf;


/**
 * 设置风速显示单位 请求 Message
 */
public class SetWindSpeedUnitReqMsg extends BasePaveMsg implements PaveMsgOutput {

    // 风速显示单位 0：km/h； 1：m/s
    private int unit;

    public SetWindSpeedUnitReqMsg(int unit) {
        this.frame = 17; // 帧号 0x11
        this.unit = unit;
    }


    @Override
    public void encode(ByteBuf buf) {
        buf.writeByte(unit); //写入一个字节数据。
    }

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }
}
