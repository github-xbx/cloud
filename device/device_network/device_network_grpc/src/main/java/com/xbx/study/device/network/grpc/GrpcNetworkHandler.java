package com.xbx.study.device.network.grpc;

import com.xbx.study.device.network.core.NetworkHandler;
import com.xbx.study.device.network.grpc.message.GrpcMessage;

/**
 * gRPC 网络处理器接口
 * 业务层实现此接口来处理 gRPC 消息
 */
public interface GrpcNetworkHandler extends NetworkHandler<GrpcMessage> {
    
    /**
     * 处理设备注册
     * 
     * @param sessionId 会话ID
     * @param deviceId 设备ID
     * @param token 认证token
     * @param deviceType 设备类型
     * @return 是否注册成功
     */
    default boolean onDeviceRegister(String sessionId, String deviceId, String token, String deviceType) {
        return true;
    }
    
    /**
     * 处理心跳
     * 
     * @param sessionId 会话ID
     * @param deviceId 设备ID
     * @return 是否处理成功
     */
    default boolean onHeartbeat(String sessionId, String deviceId) {
        return true;
    }
    
    /**
     * 设备上线回调
     * 
     * @param sessionId 会话ID
     * @param deviceId 设备ID
     */
    default void onDeviceOnline(String sessionId, String deviceId) {
    }
    
    /**
     * 设备离线回调
     * 
     * @param sessionId 会话ID
     * @param deviceId 设备ID
     */
    default void onDeviceOffline(String sessionId, String deviceId) {
    }
}
