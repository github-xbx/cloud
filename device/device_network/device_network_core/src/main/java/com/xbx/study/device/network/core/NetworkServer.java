package com.xbx.study.device.network.core;

import com.xbx.study.device.network.core.message.NetworkDownlinkMessage;

/**
 * 网路服务接口
 * 所有的网络服务都需要实现该接口
 * 接口定义了网络服务的基本方法
 */
public interface NetworkServer {

    /**
     * 开启服务
     * @throws Throwable
     */
    void start() throws Throwable;

    /**
     * 关闭服务
     * @throws Throwable
     */
    void stop() throws Throwable;

    /**
     * 服务是否运行
     * @return
     */
    boolean isRunning();


    /**
     * 发送下行数据
     * @param message
     */
    void send(NetworkDownlinkMessage message);

}
