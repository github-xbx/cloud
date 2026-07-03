package com.xbx.study.device.network.grpc;

import com.xbx.study.device.network.core.message.NetworkDownlinkMessage;
import com.xbx.study.device.network.grpc.message.GrpcMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * gRPC 通信服务示例
 * 展示如何在业务层使用 gRPC 双向通信
 */
@Service
public class GrpcCommunicationService {

    private static final Logger log = LoggerFactory.getLogger(GrpcCommunicationService.class);

    @Autowired
    private GrpcNetworkServer grpcServer;

    /**
     * 向指定设备发送消息
     * 
     * @param deviceId 设备ID
     * @param message 消息内容
     */
    public void sendToDevice(String deviceId, String message) {
        if (grpcServer == null) {
            log.error("gRPC服务器未初始化");
            return;
        }

        if (!grpcServer.isDeviceOnline(deviceId)) {
            log.warn("设备不在线: deviceId={}", deviceId);
            return;
        }

        log.info("向设备发送消息: deviceId={}, message={}", deviceId, message);
        grpcServer.sendToDevice(deviceId, message);
    }

    /**
     * 向所有设备广播消息
     * 
     * @param message 消息内容
     */
    public void broadcastToAllDevices(String message) {
        if (grpcServer == null) {
            log.error("gRPC服务器未初始化");
            return;
        }

        int deviceCount = grpcServer.getOnlineDeviceCount();
        if (deviceCount == 0) {
            log.warn("没有在线设备可广播");
            return;
        }

        log.info("向 {} 个设备广播消息: {}", deviceCount, message);
        grpcServer.broadcast(message);
    }

    /**
     * 获取所有在线设备列表
     * 
     * @return 在线设备ID列表
     */
    public List<String> getOnlineDevices() {
        if (grpcServer == null) {
            log.error("gRPC服务器未初始化");
            return List.of();
        }

        return List.copyOf(grpcServer.getOnlineDeviceIds());
    }

    /**
     * 检查设备是否在线
     * 
     * @param deviceId 设备ID
     * @return 是否在线
     */
    public boolean isDeviceOnline(String deviceId) {
        if (grpcServer == null) {
            log.error("gRPC服务器未初始化");
            return false;
        }

        return grpcServer.isDeviceOnline(deviceId);
    }

    /**
     * 获取在线设备数量
     * 
     * @return 在线设备数量
     */
    public int getOnlineDeviceCount() {
        if (grpcServer == null) {
            log.error("gRPC服务器未初始化");
            return 0;
        }

        return grpcServer.getOnlineDeviceCount();
    }
}
