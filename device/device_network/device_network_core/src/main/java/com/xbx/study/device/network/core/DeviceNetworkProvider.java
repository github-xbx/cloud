package com.xbx.study.device.network.core;

import com.xbx.study.device.network.core.enums.NetworkProtocol;

/**
 * 设备网络服务提供者接口
 * 设备接入的网络服务都要实现该接口
 *
 */
public interface DeviceNetworkProvider {
    /**
     * 定义的协议
     * @return
     */
    NetworkProtocol protocol();

    /**
     * 创建网络服务
     * @param handler
     * @return
     */
    NetworkServer create(NetworkHandler<?> handler);

}
