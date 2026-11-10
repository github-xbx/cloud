package com.network.reactor.netty.collect.server.message;

import io.netty.buffer.ByteBuf;


import java.util.Objects;


public class DeviceInfoRespMsg extends BasePaveMsg implements PaveMsgInput {

    /**
     * 版本号 0x01
     */
    private Integer version;

    /**
     * 设备类型
     */
    private Integer deviceType;

    /**
     * 设备ID
     */
    private Integer deviceId;

    /**
     * 记录类型
     */
    private Integer recodeType;

    @Override
    public void decode(ByteBuf buf) {
        this.version = Byte.toUnsignedInt(buf.readByte());
        this.deviceType = Byte.toUnsignedInt(buf.readByte());
        this.deviceId = buf.readInt();
        this.recodeType = Byte.toUnsignedInt(buf.readByte());
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getRecodeType() {
        return recodeType;
    }

    public void setRecodeType(Integer recodeType) {
        this.recodeType = recodeType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DeviceInfoRespMsg that = (DeviceInfoRespMsg) o;
        return Objects.equals(version, that.version) && Objects.equals(deviceType, that.deviceType) && Objects.equals(deviceId, that.deviceId) && Objects.equals(recodeType, that.recodeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, deviceType, deviceId, recodeType);
    }

    @Override
    public String toString() {
        return "DeviceInfoRespMsg{" +
                "version=" + version +
                ", deviceType=" + deviceType +
                ", deviceId=" + deviceId +
                ", recodeType=" + recodeType +
                ", frame=" + frame +
                ", sequence=" + sequence +
                ", crcValid=" + crcValid +
                '}';
    }
}
