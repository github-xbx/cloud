package com.network.reactor.netty.collect.server.message;

import io.netty.buffer.ByteBuf;

/**
 * 写配置数据 => 写补偿温度响应
 */
public class WriteConfigDataRespMsg extends BasePaveMsg implements PaveMsgInput {

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
}
