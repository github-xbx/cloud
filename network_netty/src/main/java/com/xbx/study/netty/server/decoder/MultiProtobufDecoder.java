package com.xbx.study.netty.server.decoder;

import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import com.xbx.study.netty.protobuf.UserProtobuf;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *   +--------+--------+-----------+
 *   | 类型ID  | 长度    | protobuf  |
 *   | 1 byte | varint32| body      |
 *   +--------+--------+-----------+
 */
public class MultiProtobufDecoder extends ByteToMessageDecoder {

    // 注册所有消息类型：typeId -> parser
    private static final Map<Integer, Parser<? extends MessageLite>> PARSERS = Map.of(
            1, UserProtobuf.User.parser()
    );

    /**
     * 解码
     * @param ctx channel context
     * @param in 输入
     * @param out 输出
     * @throws Exception 异常
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws
            Exception {
        if (in.readableBytes() < 1) return;

        in.markReaderIndex();
        int typeId = in.readByte();  // 读类型标识

        Parser<? extends MessageLite> parser = PARSERS.get(typeId);
        if (parser == null) {
            throw new IOException("未知消息类型: " + typeId);
        }

        // 剩余字节交给对应 parser 解析
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        out.add(parser.parseFrom(bytes));
    }

}
