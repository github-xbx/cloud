package com.xbx.study.netty.collect_client.protocol;


import com.xbx.study.common.CRC16IBM;
import com.xbx.study.netty.collect_client.message.PaveMessageInput;
import com.xbx.study.netty.collect_client.message.PaveMessageOutput;
import com.xbx.study.netty.collect_client.message.impl.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 消息编码器 可共享
 * 必须和 LengthFieldBasedFrameDecoder 一起使用，确保接到的 ByteBuf 消息是完整的
 *
 * @author yuchen
 */
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf, PaveMessageOutput> {

    private static final Logger log = LoggerFactory.getLogger(MessageCodecSharable.class);

    /**
     * 编码器
     * @param ctx channelHandler
     * @param msg 消息
     * @param list
     * @throws Exception
     */
    @Override
    public void encode(ChannelHandlerContext ctx, PaveMessageOutput msg, List<Object> list) throws Exception {
        ByteBuf data = null;
        try {
            ByteBuf out = ctx.alloc().buffer();
            out.writeByte(220); //0xDC
            out.writeByte(1); //version

            data = ctx.alloc().buffer();
            msg.encode(data);
            out.writeShort(data.readableBytes() + 4); //length

            out.writeByte(msg.getSequence()); //seq
            out.writeByte(msg.getFrame()); //frame
            out.writeBytes(data); //data

            byte[] crc = new byte[out.readableBytes()];//CRC
            out.readBytes(crc);
            CRC16IBM crc16 = new CRC16IBM();
            crc16.update(crc, 0, crc.length);
            long crcVal = crc16.getValue();
            out.writeShort((int)crcVal);

            out.resetReaderIndex();
            list.add(out);
        } finally {
            if(data != null) data.release();
        }
    }

    /**
     * 解码器
     * @param ctx channelHandler
     * @param in 输入，netty server 发送的数据（ByteBuf）
     * @param out 输出，解码后的数据
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        ByteBuf buf = null;
        ByteBuf _data = null;
        ByteBuf data = null;
        try {
            int magic = Byte.toUnsignedInt(in.readByte());
            if (magic != 220) return;  //0xDC
            int version = Byte.toUnsignedInt(in.readByte());
            int len = Short.toUnsignedInt(in.readShort());
            buf = in.readBytes(len);

            int seq = Byte.toUnsignedInt(buf.readByte());
            int frame = Byte.toUnsignedInt(buf.readByte());
            _data = buf.readBytes(len - 2); // seq+frame=2
            data = _data.readBytes(_data.readableBytes() - 2);// -crc

            long crcVal = Short.toUnsignedInt(_data.readShort());
            in.resetReaderIndex();
            byte[] crc = new byte[len -2 + 4]; // -crc+头
            in.readBytes(crc);
            CRC16IBM crc16 = new CRC16IBM();
            crc16.update(crc, 0, crc.length);
            long crcValCalc = crc16.getValue();

            log.debug("magic={},version={},len={},seq={},frame={},crc={}", magic, version, len, seq, frame, crcVal);

            FrameTypeEnum frameTypeEnum = FrameTypeEnum.of(frame, Direction.IN);
            if (frameTypeEnum != null) {
                PaveMessageInput paveMsg = (PaveMessageInput)frameTypeEnum.clazz.newInstance();
                paveMsg.setFrame(frame);
                paveMsg.setSequence(seq);
                paveMsg.setCrcValid(crcVal == crcValCalc);
                paveMsg.decode(data);
                out.add(paveMsg);
            }
        } finally {
            if(buf != null) buf.release();
            if(_data != null) _data.release();
            if(data != null) data.release();
        }
    }

    public enum FrameTypeEnum {
        HEARTBEAT_REQ(1, Direction.OUT, HeartBeatRequest.class,"心跳请求"),
        DEVICE_INFO_REQ(2, Direction.OUT, DeviceInfoRequest.class, "设备描述读取请求"),
        DEVICE_INFO_RESP(2, Direction.IN, DeviceInfoResponse.class, "设备描述读取响应"),
        RECORD_READ_REQ(3, Direction.OUT, ReadRecordRequest.class, "记录读取请求"),
        RECORD_READ_RESP(3, Direction.IN, ReadRecordResponse.class, "记录读取响应"),
        REAL_TIME_REQ(4, Direction.OUT, RealTimeRequest.class, "实时数据请求"),
        REAL_TIME_RESP(4, Direction.IN, RealTimeResponse.class, "实时数据响应"),
        WIND_SPEED_UNIT_REQ(17, Direction.OUT, SetWindSpeedUnitRequest.class, "风速单位请求"),
        WIND_SPEED_UNIT_RESP(17, Direction.IN, SetWindSpeedUnitResponse.class, "风速单位响应"),
        WRITE_CONFIG_REQ(16, Direction.OUT, WriteConfigDataRequest.class,"写配置"),
        WRITE_CONFIG_RESP(16, Direction.IN, WriteConfigDataResponse.class, "<写配置>"),
        ;
        int frame;
        Direction direction;
        Class<?> clazz;
        String desc;
        FrameTypeEnum(int frame, Direction direction, Class<?> clazz, String desc) {
            this.frame = frame;
            this.direction = direction;
            this.desc = desc;
            this.clazz = clazz;
        }

        public static FrameTypeEnum of(int frame, Direction direction) {
            for (FrameTypeEnum v : values()) {
                if(v.frame == frame && v.direction == direction) return v;
            }
            return null;
        }
    }

    public enum Direction {
        IN, OUT;
    }
}
