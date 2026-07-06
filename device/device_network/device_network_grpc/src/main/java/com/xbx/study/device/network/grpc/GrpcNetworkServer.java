package com.xbx.study.device.network.grpc;

import com.xbx.study.device.network.core.NetworkServer;
import com.xbx.study.device.network.core.message.BaseMessage;
import com.xbx.study.device.network.core.message.NetworkDownlinkMessage;
import com.xbx.study.device.network.grpc.handler.GrpcNetworkHandler;
import com.xbx.study.device.network.grpc.proto.DeviceMessage;
import com.xbx.study.device.network.grpc.proto.MessageType;
import com.xbx.study.device.network.grpc.service.DeviceServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;

public class GrpcNetworkServer implements NetworkServer {

    private static final Logger log = LoggerFactory.getLogger(GrpcNetworkServer.class);

    private final GrpcSessionManager sessionManager = GrpcSessionManager.getInstance();
    private final int port;
    private final GrpcNetworkHandler handler;
    private Server server;
    private volatile boolean running;

    public GrpcNetworkServer(int port, GrpcNetworkHandler handler) {
        this.port = port;
        this.handler = handler;
    }

    @Override
    public void start() throws Throwable {
        log.info("启动gRPC服务器，端口: {}", port);
        
        server = ServerBuilder.forPort(port)
                .addService(new DeviceServiceImpl(handler))
                .build()
                .start();
        
        running = true;
        log.info("gRPC服务器启动成功，端口: {}", port);
        
        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("JVM关闭，停止gRPC服务器");
            try {
                stop();
            } catch (Throwable e) {
                log.error("停止gRPC服务器失败", e);
            }
        }));
    }

    @Override
    public void stop() throws Throwable {
        log.info("停止gRPC服务器...");
        running = false;
        
        if (server != null) {
            server.shutdown();
            log.info("gRPC服务器已停止");
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void send(NetworkDownlinkMessage message) {
        if (message == null) {
            log.warn("发送消息失败：消息为空");
            return;
        }

        String sessionId = message.getSessionId();
        String deviceId = message.getDeviceId();

        // 优先使用 sessionId
        if (sessionId != null) {
            sendToSession(sessionId, message.getPayload());
        }
        // 其次使用 deviceId
        else if (deviceId != null) {
            sendToDevice(deviceId, message.getPayload());
        }
        else {
            log.warn("发送消息失败：sessionId和deviceId都为空");
        }
    }

    /**
     * 向指定会话发送消息
     */
    public void sendToSession(String sessionId, String payload) {
        if (sessionId == null || payload == null) {
            log.warn("发送消息失败：sessionId或payload为空");
            return;
        }

        StreamObserver<?> observer = sessionManager.getObserver(sessionId);
        if (observer == null) {
            log.warn("发送消息失败：找不到会话的observer, sessionId={}", sessionId);
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            StreamObserver<DeviceMessage> deviceObserver = (StreamObserver<DeviceMessage>) observer;
            
            DeviceMessage message = DeviceMessage.newBuilder()
                    .setMessageId(UUID.randomUUID().toString())
                    .setSessionId(sessionId)
                    .setType(MessageType.COMMAND)
                    .setPayload(payload)
                    .setTimestamp(System.currentTimeMillis())
                    .build();
            
            deviceObserver.onNext(message);
            log.info("发送gRPC消息: sessionId={}, payload={}", sessionId, payload);
        } catch (Exception e) {
            log.error("发送gRPC消息失败: sessionId={}", sessionId, e);
        }
    }

    /**
     * 向指定设备发送消息
     */
    public void sendToDevice(String deviceId, String payload) {
        if (deviceId == null || payload == null) {
            log.warn("发送消息失败：deviceId或payload为空");
            return;
        }

        if (!sessionManager.isDeviceOnline(deviceId)) {
            log.warn("发送消息失败：设备不在线, deviceId={}", deviceId);
            return;
        }

        Set<String> sessionIds = sessionManager.getSessionIds(deviceId);
        if (sessionIds == null || sessionIds.isEmpty()) {
            log.warn("发送消息失败：找不到设备的会话, deviceId={}", deviceId);
            return;
        }

        for (String sessionId : sessionIds) {
            sendToSession(sessionId, payload);
        }
    }

    /**
     * 向所有设备广播消息
     */
    public void broadcast(String payload) {
        if (payload == null) {
            log.warn("广播消息失败：payload为空");
            return;
        }

        int deviceCount = sessionManager.getOnlineDeviceCount();
        if (deviceCount == 0) {
            log.warn("广播消息失败：没有在线设备");
            return;
        }

        log.info("向 {} 个设备广播消息: {}", deviceCount, payload);
        
        sessionManager.getActiveSessionIds().forEach(sessionId -> {
            sendToSession(sessionId, payload);
        });
    }

    /**
     * 获取在线设备ID列表
     */
    public Set<String> getOnlineDeviceIds() {
        return sessionManager.getOnlineDeviceIds();
    }

    /**
     * 检查设备是否在线
     */
    public boolean isDeviceOnline(String deviceId) {
        return sessionManager.isDeviceOnline(deviceId);
    }

    /**
     * 获取在线设备数量
     */
    public int getOnlineDeviceCount() {
        return sessionManager.getOnlineDeviceCount();
    }

    /**
     * 获取 gRPC 处理器
     */
    public GrpcNetworkHandler getHandler() {
        return handler;
    }
}
