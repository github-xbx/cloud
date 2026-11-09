package com.network.reactor.netty.collect.client.protocol;

import com.network.reactor.netty.collect.client.message.PaveMessageInput;
import com.network.reactor.netty.collect.client.message.PaveMessageOutput;
import com.network.reactor.netty.client.collect.message.impl.*;
import com.network.reactor.netty.collect.client.message.impl.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

/**
 * 消息转化 数据编码器
 */
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf, PaveMessageOutput> {

    /**
     *
     * 编码 output ->  ByteBuf
     */
    @Override
    protected void encode(ChannelHandlerContext context, PaveMessageOutput output, List<Object> list) throws Exception {
        ByteBuf data = null;
        try {
            //alloc() 返回与这条 channel 绑定的 ByteBufAllocator buffer() 池化，direct/heap 由 allocator 决定
            ByteBuf out = context.alloc().buffer();

            out.writeByte(220); //0xDC   1Byte
            out.writeByte(1); //version  1Byte

            data = context.alloc().buffer();
            output.encode(data);             //具体message 的编码 操作，主要是操作数据体
            out.writeShort(data.readableBytes() + 4);    // +4 主要是  seq 和 frame 个占一个字节 + crc验证 站2个字节

            out.writeByte(output.getSequence()); //seq
            out.writeByte(output.getFrame());  // frame
            out.writeBytes(data);

            //crc

            byte[] bytes = new byte[out.readableBytes()];
            out.readBytes(bytes);                      //这里读取完成之后 Byte会把指针执行（readerIndex: 0 → 末尾）如在读取read时会报错
            Long crc = crc(bytes, 0, bytes.length);
            out.writeShort(crc.intValue());

            out.resetReaderIndex(); //因为从未 mark，回到默认的 0
            list.add(out);

        }catch (Exception e){
            if(data != null)
                data.release();
        }
    }

    /**
     *
     * 解码 ByteBuf -> output
     */
    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf byteBuf, List<Object> list) throws Exception {
        ByteBuf buf = null;
        ByteBuf _data = null;
        ByteBuf data = null;
        try {
            int magic = Byte.toUnsignedInt(byteBuf.readByte());
            if (magic != 220) //校验魔数
                return;  //0xDC
            int version = Byte.toUnsignedInt(byteBuf.readByte());
            int length = byteBuf.readUnsignedShort();
            buf = byteBuf.readBytes(length);

            int seq = Byte.toUnsignedInt(buf.readByte());
            int frame = Byte.toUnsignedInt(buf.readByte());

            _data = buf.readBytes(length - 4);
            int crc = buf.readUnsignedShort();

            buf.resetReaderIndex();

            // crc 校验
            byte[] bytes = new byte[length - 2 + 4];
            byteBuf.readBytes(bytes);

            Long crc1 = crc(bytes, 0, bytes.length);

            FrameTypeEnum frameTypeEnum = FrameTypeEnum.of(frame, Direction.IN);
            if (frameTypeEnum != null){
                PaveMessageInput input = (PaveMessageInput) frameTypeEnum.clazz.newInstance();

                input.setFrame(frame);
                input.setSequence(seq);
                input.setCrcValid(crc1 == crc);
                input.decode(_data);
                list.add(input);

            }
        } catch (Exception e) {
            if(buf != null) buf.release();
            if(_data != null) _data.release();
            if(data != null) data.release();
        }

    }



    private Long crc(byte[] data, int off, int len){
        Long wCRCin = 0L;
        for(int i = off; i < off + len; i++){
            int b = data[i];
            wCRCin ^= b & 255;

            for(int j = 0; j < 8; ++j) {
                if ((wCRCin & 1) != 0) {
                    wCRCin >>= 1;
                    wCRCin ^= 40961;
                } else {
                    wCRCin >>= 1;
                }
            }
        }
        return wCRCin;
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
