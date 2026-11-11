package com.network.reactor.netty.collect.client.message.impl;


import com.network.reactor.netty.collect.client.message.BasePaveMessage;
import com.network.reactor.netty.collect.client.message.PaveMessageInput;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceInfoResponse extends BasePaveMessage implements PaveMessageInput {

    private static final Logger logger = LoggerFactory.getLogger(DeviceInfoResponse.class);
    private Integer type;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    /**
     * 解码
     * @param buf 输入的ByteBuf数据
     */
    @Override
    public void decode(ByteBuf buf) {
        type = Byte.toUnsignedInt(buf.readByte());
        logger.info("设备描述读取响应 => type ：{}", type);
    }
}
