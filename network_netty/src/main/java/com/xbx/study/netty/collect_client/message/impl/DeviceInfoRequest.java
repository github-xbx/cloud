package com.xbx.study.netty.collect_client.message.impl;

import com.xbx.study.netty.collect_client.message.BasePaveMessage;
import com.xbx.study.netty.collect_client.message.PaveMessageOutput;
import io.netty.buffer.ByteBuf;

import java.util.Objects;

/**
 * 设备信息请求
 */
public class DeviceInfoRequest extends BasePaveMessage implements PaveMessageOutput {
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

    /**
     * 编码
     * @param buf
     */
    @Override
    public void encode(ByteBuf buf) {
        buf.writeByte(version);
        buf.writeByte(deviceType);
        buf.writeInt(deviceId);
        buf.writeByte(recodeType);
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceInfoRequest that = (DeviceInfoRequest) o;
        return Objects.equals(version, that.version) && Objects.equals(deviceType, that.deviceType) && Objects.equals(deviceId, that.deviceId) && Objects.equals(recodeType, that.recodeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, deviceType, deviceId, recodeType);
    }

    @Override
    public String toString() {
        return "DeviceInfoRequest{" +
                "version=" + version +
                ", deviceType=" + deviceType +
                ", deviceId=" + deviceId +
                ", recodeType=" + recodeType +
                ", crcValid=" + crcValid +
                ", sequence=" + sequence +
                ", frame=" + frame +
                '}';
    }
}
