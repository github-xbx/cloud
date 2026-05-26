package com.xbx.study.netty.collect_client.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 协议帧解码器
 * <p>
 * 协议帧格式：
 * <pre>
 * +--------+--------+----------+-----------+
 * | 魔数    | 保留    | 数据长度  | 数据体     |
 * +--------+--------+----------+-----------+
 * | (1字节) | (1字节) | (2字节)  | (N字节)    |
 * +--------+--------+----------+-----------+
 * </pre>
 * <ul>
 *   <li><b>魔数</b> — 固定 {@code 0xDC}，用于校验是否为合法数据</li>
 *   <li><b>保留</b> — 保留字段，暂未使用</li>
 *   <li><b>数据长度</b> — 后续数据体的长度，大端序</li>
 *   <li><b>数据体</b> — 实际业务数据</li>
 * </ul>
 * <p>
 * 解码流程：
 * <ol>
 *   <li>校验魔数字节，不匹配则整包丢弃</li>
 *   <li>读取数据长度字段，调用 {@link LengthFieldBasedFrameDecoder} 完成半包/粘包处理</li>
 * </ol>
 */
public class ProtocolFrameDecoder extends LengthFieldBasedFrameDecoder {

    private static final Logger log = LoggerFactory.getLogger(ProtocolFrameDecoder.class);

    private static final int MAGIC_NUMBER = 0xDC;


    /**
     * <ul>
     *     <li>maxFrameLength (最大帧长度): 一个安全阀，用于设置允许的最大消息长度</li>
     *     <li>lengthFieldOffset (长度字段偏移量): 指定长度字段本身在整个消息中的起始位置。例如，长度字段从消息的第 3 个字节开始，偏移量即为 2（索引从 0 开始</li>
     *     <li>lengthFieldLength (长度字段长度): 长度字段所占的字节数。常见值为 2 或 4。解码器会读取这么多个字节，并将其解析为一个数值</li>
     *     <li>lengthAdjustment (长度调整值): 用于修正长度字段值的计算补偿。它等于 (整个消息的总长度) - (长度字段的值)</li>
     *     <ul>
     *         <li>场景一：若协议定义 Length 字段仅表示“消息体”的长度（不包含 Length 字段本身），则 lengthAdjustment = 0</li>
     *         <li>场景二：若 Length 字段表示“整个消息”的长度（包含头部的 Length 字段），由于读取 Length 字段时已经消耗掉了其自身的几个字节，需要告知解码器从 Length 字段的值中减去这些字节，即 lengthAdjustment = -lengthFieldLength</li>
     *     </ul>
     *     <li>initialBytesToStrip (初始剥离字节数): 指定解码后，要从最终的消息帧开头剥离的字节数。通常设为 lengthFieldLength，以移除长度字段，只留下消息体交给上层业务Handler</li>
     *     <li>failFast (快速失败): 用于指定是否快速失败，通常为false</li>
     *
     * </ul>
     *
     */
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
