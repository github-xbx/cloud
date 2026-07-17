package com.xbx.study.device.network.grpc.handler;

import com.google.protobuf.Message;
import com.xbx.study.device.network.core.NetworkHandler;
import com.xbx.study.device.network.core.message.BaseMessage;
import com.xbx.study.device.network.grpc.proto.DeviceData;
import io.grpc.stub.StreamObserver;

/**
 * gRPC 网络处理器接口
 * 业务层实现此接口来处理 gRPC 消息
 */
public interface GrpcNetworkHandler extends NetworkHandler {
    
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
     * @param type 离线类型
     */
    default void onDeviceOffline(String sessionId, String deviceId, String type) {
    }


    @Override
    default void onMessage(BaseMessage message){
    }


    default void onMessage(Message msg, StreamObserver<? extends Message> responseObserver){
    }

    /**
     * 处理上送的数据
     */
    default void onUpData(DeviceData data){

    }


}
