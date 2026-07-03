package com.xbx.study.device.network.grpc;

import com.xbx.study.device.network.grpc.message.GrpcMessage;
import com.xbx.study.device.network.grpc.proto.*;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * gRPC 设备服务实现
 * 处理设备的 gRPC 请求
 */
public class DeviceServiceImpl extends DeviceServiceGrpc.DeviceServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(DeviceServiceImpl.class);

    private final GrpcSessionManager sessionManager = GrpcSessionManager.getInstance();
    private final GrpcNetworkHandler handler;

    public DeviceServiceImpl(GrpcNetworkHandler handler) {
        this.handler = handler;
    }

    /**
     * 双向流式通信
     */
    @Override
    public StreamObserver<DeviceMessage> communicate(StreamObserver<DeviceMessage> responseObserver) {
        String sessionId = UUID.randomUUID().toString();
        
        // 注册会话
        sessionManager.registerSession(sessionId, responseObserver);
        
        log.info("gRPC双向流建立: sessionId={}", sessionId);

        return new StreamObserver<DeviceMessage>() {
            private String deviceId;

            @Override
            public void onNext(DeviceMessage message) {
                log.info("收到gRPC消息: sessionId={}, deviceId={}, type={}", 
                        sessionId, message.getDeviceId(), message.getType());
                
                // 绑定设备
                if (message.getDeviceId() != null && !message.getDeviceId().isEmpty()) {
                    if (this.deviceId == null) {
                        this.deviceId = message.getDeviceId();
                        sessionManager.bindDevice(sessionId, this.deviceId);
                        
                        // 通知设备上线
                        if (handler != null) {
                            handler.onDeviceOnline(sessionId, this.deviceId);
                        }
                    }
                }
                
                // 转换为 GrpcMessage 并传递给 handler
                GrpcMessage grpcMessage = convertToGrpcMessage(sessionId, message);
                if (handler != null) {
                    handler.onMessage(sessionId, grpcMessage);
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("gRPC双向流错误: sessionId={}", sessionId, t);
                
                // 通知设备离线
                if (handler != null && this.deviceId != null) {
                    handler.onDeviceOffline(sessionId, this.deviceId);
                }
                
                sessionManager.unregisterSession(sessionId);
            }

            @Override
            public void onCompleted() {
                log.info("gRPC双向流完成: sessionId={}", sessionId);
                
                // 通知设备离线
                if (handler != null && this.deviceId != null) {
                    handler.onDeviceOffline(sessionId, this.deviceId);
                }
                
                sessionManager.unregisterSession(sessionId);
                responseObserver.onCompleted();
            }
        };
    }

    /**
     * 客户端流式通信（设备上报数据）
     */
    @Override
    public StreamObserver<DeviceData> report(StreamObserver<ReportResponse> responseObserver) {
        String sessionId = UUID.randomUUID().toString();
        
        log.info("gRPC客户端流建立: sessionId={}", sessionId);

        return new StreamObserver<DeviceData>() {
            private String deviceId;

            @Override
            public void onNext(DeviceData data) {
                log.info("收到设备数据: sessionId={}, deviceId={}, dataType={}", 
                        sessionId, data.getDeviceId(), data.getDataType());
                
                // 绑定设备
                if (data.getDeviceId() != null && !data.getDeviceId().isEmpty()) {
                    if (this.deviceId == null) {
                        this.deviceId = data.getDeviceId();
                        sessionManager.bindDevice(sessionId, this.deviceId);
                    }
                }
                
                // 转换并处理
                GrpcMessage grpcMessage = convertDataToGrpcMessage(sessionId, data);
                if (handler != null) {
                    handler.onMessage(sessionId, grpcMessage);
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("gRPC客户端流错误: sessionId={}", sessionId, t);
            }

            @Override
            public void onCompleted() {
                log.info("gRPC客户端流完成: sessionId={}", sessionId);
                responseObserver.onNext(ReportResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Report completed")
                        .build());
                responseObserver.onCompleted();
            }
        };
    }

    /**
     * 服务端流式通信（服务端下发命令）
     */
    @Override
    public void sendCommand(CommandRequest request, StreamObserver<CommandResponse> responseObserver) {
        log.info("收到命令请求: commandId={}, deviceId={}, command={}", 
                request.getCommandId(), request.getDeviceId(), request.getCommand());
        
        // 处理命令
        // 这里可以调用业务层处理命令
        
        responseObserver.onNext(CommandResponse.newBuilder()
                .setCommandId(request.getCommandId())
                .setSuccess(true)
                .setResult("Command received")
                .setMessage("Processing...")
                .build());
        responseObserver.onCompleted();
    }

    /**
     * 一元调用（设备注册）
     */
    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        String sessionId = UUID.randomUUID().toString();
        
        log.info("设备注册: deviceId={}, token={}, deviceType={}", 
                request.getDeviceId(), request.getToken(), request.getDeviceType());
        
        // 调用业务层处理注册
        boolean success = true;
        if (handler != null) {
            success = handler.onDeviceRegister(
                    sessionId, 
                    request.getDeviceId(), 
                    request.getToken(), 
                    request.getDeviceType()
            );
        }
        
        if (success) {
            // 绑定设备
            sessionManager.bindDevice(sessionId, request.getDeviceId());
            
            // 注册会话（使用 null observer，因为一元调用没有持久连接）
            sessionManager.registerSession(sessionId, null);
            
            // 通知设备上线
            if (handler != null) {
                handler.onDeviceOnline(sessionId, request.getDeviceId());
            }
            
            responseObserver.onNext(RegisterResponse.newBuilder()
                    .setSuccess(true)
                    .setSessionId(sessionId)
                    .setMessage("Register success")
                    .build());
        } else {
            responseObserver.onNext(RegisterResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Register failed: authentication error")
                    .build());
        }
        responseObserver.onCompleted();
    }

    /**
     * 一元调用（心跳）
     */
    @Override
    public void heartbeat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
        log.debug("收到心跳: deviceId={}, sessionId={}", request.getDeviceId(), request.getSessionId());
        
        // 调用业务层处理心跳
        boolean success = true;
        if (handler != null) {
            success = handler.onHeartbeat(request.getSessionId(), request.getDeviceId());
        }
        
        responseObserver.onNext(HeartbeatResponse.newBuilder()
                .setSuccess(success)
                .setServerTimestamp(System.currentTimeMillis())
                .build());
        responseObserver.onCompleted();
    }

    /**
     * 转换 DeviceMessage 为 GrpcMessage
     */
    private GrpcMessage convertToGrpcMessage(String sessionId, DeviceMessage message) {
        GrpcMessage grpcMessage = new GrpcMessage();
        grpcMessage.setMessageId(message.getMessageId());
        grpcMessage.setDeviceId(message.getDeviceId());
        grpcMessage.setSessionId(sessionId);
        grpcMessage.setType(message.getType().name());
        grpcMessage.setPayload(message.getPayload());
        grpcMessage.setTimestamp(message.getTimestamp());
        return grpcMessage;
    }

    /**
     * 转换 DeviceData 为 GrpcMessage
     */
    private GrpcMessage convertDataToGrpcMessage(String sessionId, DeviceData data) {
        GrpcMessage grpcMessage = new GrpcMessage();
        grpcMessage.setDeviceId(data.getDeviceId());
        grpcMessage.setSessionId(sessionId);
        grpcMessage.setType("DATA");
        grpcMessage.setPayload(data.getPayload());
        grpcMessage.setTimestamp(data.getTimestamp());
        return grpcMessage;
    }
}
