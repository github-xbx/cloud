package com.network.reactor.netty.collect.server.message;

import io.netty.buffer.ByteBuf;

/**
 * 写配置数据
 * 下发补偿温度
 */
public class WriteConfigDataReqMsg extends BasePaveMsg implements PaveMsgOutput{

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


    public WriteConfigDataReqMsg(float startTemperature, float endTemperature, float temperature) {
        this.frame = 16;    // 帧号 0x10
        this.startTemperature = startTemperature;
        this.endTemperature = endTemperature;
        this.temperature = temperature;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeFloat(startTemperature);  //四个字节 开始补偿温度值
        buf.writeFloat(endTemperature);    //四个字节 结束补偿温度值
        buf.writeFloat(temperature);       //四个字节 补偿温度
    }

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
}
