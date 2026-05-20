package com.xbx.study.netty.collect_client.message;

import com.xbx.study.netty.collect_client.domain.PaveRecord;
import io.netty.buffer.ByteBuf;

public abstract class BasePaveMessage {

    protected int frame;
    protected int sequence;
    protected boolean crcValid;

    public int getFrame() {
        return frame;
    }

    public int getSequence() {
        return sequence;
    }

    public boolean isCrcValid() {
        return crcValid;
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public void setCrcValid(boolean crcValid) {
        this.crcValid = crcValid;
    }

    /**
     * 解析数据 ByteBuf => PaveRecord
     * @param buf 输入的ByteBuf数据
     * @return 转换之后的PaveRecord对象
     */
    protected PaveRecord parseData(ByteBuf buf){

        long rtc = buf.readUnsignedInt();
        int millisecond = buf.readUnsignedShort();
        long timestamp = (rtc + 946656000L) * 1000 + millisecond;

        int locationStatus = buf.readUnsignedByte();
        int temperatureStatus = buf.readUnsignedByte();
        int waveStatus = buf.readUnsignedByte();

        double lng = buf.readDouble();
        double lat = buf.readDouble();

        double speed = buf.readFloat();
        double direction = buf.readFloat();
        double temperature = buf.readFloat();

        double waveAX = buf.readFloat();
        double waveAY = buf.readFloat();
        double waveAZ = buf.readFloat();

        return new PaveRecord(timestamp, locationStatus, temperatureStatus, waveStatus,
                lng, lat, speed, direction, temperature, waveAX, waveAY, waveAZ);
    }


    /**
     * 生成数据 PaveRecord => ByteBuf
     * @param buf 输出的ByteBuf数据
     * @param pave 输入的PaveRecord对象
     */
    protected void generateData(ByteBuf buf, PaveRecord pave) {

        // 1. 计算RTC秒数（从2000-01-01 08:00:00开始的秒数）
        final long BASE_MILLIS = 946656000L * 1000L;  // 基准时间毫秒数

        // 2. 计算从基准时间开始的毫秒数
        long deltaMillis = pave.id() - BASE_MILLIS;

        // 3. 分离秒和毫秒部分
        long rtcSeconds = deltaMillis / 1000L;       // 秒数部分
        int milliseconds = (int) (deltaMillis % 1000L); // 毫秒部分

        // 4. 写入缓冲区（与解析代码对应）
        buf.writeInt((int) rtcSeconds);      // 写入4个字节无符号整数（rtc）
        buf.writeShort(milliseconds);        // 写入2个字节无符号整数（毫秒）

        buf.writeByte(pave.locationStatus());
        buf.writeByte(pave.temperatureStatus());
        buf.writeByte(pave.waveStatus());

        buf.writeDouble(pave.longitude());
        buf.writeDouble(pave.latitude());


        buf.writeFloat(pave.speed().floatValue());
        buf.writeFloat(pave.direction().floatValue());
        buf.writeFloat(pave.temperature().floatValue());

        buf.writeFloat(pave.waveAX().floatValue());
        buf.writeFloat(pave.waveAY().floatValue());
        buf.writeFloat(pave.waveAZ().floatValue());

    }

}
