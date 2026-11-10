package com.network.reactor.netty.collect.server.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ProtocolFrameDecoder extends LengthFieldBasedFrameDecoder {


    private static final int MAGIC_NUMBER = 0xDC;


    /**
     * 定义数据报文协议
     * maxFrameLength 数据帧最大数据量 204800 位
     * lengthFieldOffset 长度字段偏移量 2 个字节
     * lengthFieldLength 长度字段的长度 2 个字节 short
     * lengthAdjustment 长度调整值 0
     * initialBytesToStrip 初始剥离的字段 0
     */
    public ProtocolFrameDecoder() {
        this(204800, 2, 2, 0, 0);
    }

    public ProtocolFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment,  int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }


    /**
     * 仅作 通信协议中 魔数 的校验 长度校验 还是交给父类去做
     *
     */
    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {

        //判断 ByteBuf 中是否有数据，readableBytes() 表示 ByteBuf 中有多少个字节可以读，是数值
        if (in.readableBytes() < 1){
            return null;
        }

        // 读取可读区的第一个字节  getUnsignedByte()=>某位置上的字节值
        int magicNumber = in.getUnsignedByte(in.readerIndex());
        if (magicNumber != MAGIC_NUMBER){
            in.skipBytes(in.readableBytes()); // 丢弃不合法的数据
            return null; // 返回null表示无法解码
        }

        // 调用父类的方法进行长度字段解码
        return super.decode(ctx,in);

    }
}
