package com.xbx.study.netty.server.decoder;

import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import com.xbx.study.netty.protobuf.UserProtobuf;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 自定义 Protobuf 编解码器，支持多种消息类型的自动路由。
 *
 * <h3>协议格式</h3>
 * <pre>
 * +----------+----------+-----------+
 * | 长度      | 类型ID   | protobuf  |
 * | varint32 | 1 byte   | body      |
 * +----------+----------+-----------+
 * </pre>
 *
 * <h3>解码流程（入站）</h3>
 * <pre>
 * 原始数据: [varint32长度][类型ID][protobuf body]
 *     ↓
 * ProtobufVarint32FrameDecoder（剥离长度字段）
 *     ↓
 * 输出: [类型ID][protobuf body]
 *     ↓
 * 读取类型ID → 选择 Parser → 解析 protobuf
 *     ↓
 * MessageLite 对象
 * </pre>
 *
 * <h3>编码流程（出站）</h3>
 * <pre>
 * MessageLite 对象
 *     ↓
 * 获取类型ID → 序列化为字节数组
 *     ↓
 * 写入: [varint32长度][类型ID][body]
 * </pre>
 *
 * <p><strong>注意：</strong>解码时 {@link io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder}
 * 已经剥离了长度字段，因此本类只需处理 [类型ID][protobuf body] 部分。</p>
 *
 * @see io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder
 * @see com.google.protobuf.MessageLite
 */

public class ProtobufMessageCodec extends ByteToMessageCodec<MessageLite> {


    /**
     * 编码（出站）：将 Protobuf 消息编码为字节流。
     *
     * <p>编码格式：[varint32长度][类型ID][protobuf body]</p>
     *
     * @param ctx 通道上下文
     * @param msg 待编码的 Protobuf 消息
     * @param out 输出字节缓冲区
     * @throws Exception 编码异常
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, MessageLite msg, ByteBuf out) throws Exception {




    }

    /**
     * 解码（入站）：将字节流解码为 Protobuf 消息。
     *
     * <p>输入格式：[类型ID][protobuf body]（长度字段已被 {@code ProtobufVarint32FrameDecoder} 剥离）</p>
     *
     * @param ctx 通道上下文
     * @param in  输入字节缓冲区
     * @param out 输出列表，解码后的消息添加到此列表
     * @throws Exception 解码异常
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        if (in.readableBytes() < 1) return;

        int typeId = in.readByte();  // 读类型标识


        // todo 没有写完 考虑 spi 方式
        Parser<? extends MessageLite> parser = UserProtobuf.User.parser();
        if (parser == null) {
            throw new IOException("未知消息类型: " + typeId);
        }

        // 剩余字节交给对应 parser 解析
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        out.add(parser.parseFrom(bytes));

    }
}
