package com.network.reactor.netty.collect.server.message;

import io.netty.buffer.ByteBuf;


/**
 * 设置风速显示单位 响应 Message
 */
public class SetWindSpeedUnitRespMsg extends BasePaveMsg implements PaveMsgInput {

    // 0x00：收到并执行
    private int ack;

    @Override
    public void decode(ByteBuf buf) {
        //读取一个字节的数据
        this.ack = buf.readUnsignedByte();
    }

    public int getAck() {
        return ack;
    }

    public void setAck(int ack) {
        this.ack = ack;
    }

    @Override
    public String toString() {
        return "SetWindSpeedUnitRespMsg{" +
                "ack=" + ack +
                ", frame=" + frame +
                ", sequence=" + sequence +
                ", crcValid=" + crcValid +
                '}';
    }
}
