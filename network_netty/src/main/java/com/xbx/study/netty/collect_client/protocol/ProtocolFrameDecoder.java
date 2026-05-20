package com.xbx.study.netty.collect_client.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 协议帧解码器
 */
public class ProtocolFrameDecoder extends LengthFieldBasedFrameDecoder {

    private static final Logger log = LoggerFactory.getLogger(ProtocolFrameDecoder.class);

    private static final int MAGIC_NUMBER = 0xDC;

    public ProtocolFrameDecoder() {
        this(204800, 2, 2, 0, 0);
    }

    public ProtocolFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        // 检查魔数
        if (in.readableBytes() < 1) { // 假设魔数的长度为4个字节
            return null; // 数据不够，不进行解码
        }
        int magicNumber = in.getUnsignedByte(in.readerIndex());
        //log.info("hashcode: {}, magic: {}", in.hashCode(), magicNumber);
        if (magicNumber != MAGIC_NUMBER) {
            log.error("丢弃不合法的数据 {}B", in.readableBytes());
            in.skipBytes(in.readableBytes()); // 丢弃不合法的数据
            return null; // 返回null表示无法解码
        }

        // 调用父类的方法进行长度字段解码
        return super.decode(ctx, in);
    }
}
