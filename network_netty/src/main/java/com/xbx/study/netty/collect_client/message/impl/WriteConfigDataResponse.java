package com.xbx.study.netty.collect_client.message.impl;

import com.xbx.study.netty.collect_client.message.BasePaveMessage;
import com.xbx.study.netty.collect_client.message.PaveMessageInput;
import io.netty.buffer.ByteBuf;

import java.util.Objects;

public class WriteConfigDataResponse extends BasePaveMessage implements PaveMessageInput {


    /**
     * 开始补偿温度
     */
    private float startTemperature;

    /**
     * 结束补偿温度
     */
    private float endTemperature;

    /**
     * 补偿温度
     */
    private float temperature;

    public float getStartTemperature() {
        return startTemperature;
    }

    public void setStartTemperature(float startTemperature) {
        this.startTemperature = startTemperature;
    }

    public float getEndTemperature() {
        return endTemperature;
    }

    public void setEndTemperature(float endTemperature) {
        this.endTemperature = endTemperature;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    @Override
    public void decode(ByteBuf buf) {
//        this.startTemperature = buf.readUnsignedInt();
//        this.endTemperature = buf.readUnsignedInt();
//        this.temperature = buf.readUnsignedInt();
        this.startTemperature = buf.readFloat();
        this.endTemperature = buf.readFloat();
        this.temperature = buf.readFloat();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WriteConfigDataResponse that = (WriteConfigDataResponse) o;
        return Float.compare(startTemperature, that.startTemperature) == 0 && Float.compare(endTemperature, that.endTemperature) == 0 && Float.compare(temperature, that.temperature) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTemperature, endTemperature, temperature);
    }

    @Override
    public String toString() {
        return "WriteConfigDataResponse{" +
                "startTemperature=" + startTemperature +
                ", endTemperature=" + endTemperature +
                ", temperature=" + temperature +
                ", frame=" + frame +
                ", sequence=" + sequence +
                ", crcValid=" + crcValid +
                '}';
    }
}
