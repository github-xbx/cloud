package com.xbx.study.device.network.grpc.message;

import com.xbx.study.device.network.core.message.BaseMessage;

/**
 * gRPC 消息实现类
 * 实现 BaseMessage 接口，用于封装 gRPC 接收到的消息
 */
public class GrpcMessage implements BaseMessage {

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息载荷
     */
    private String payload;

    /**
     * 时间戳
     */
    private long timestamp;

    public GrpcMessage() {
    }

    public GrpcMessage(String deviceId, String sessionId, String payload) {
        this.deviceId = deviceId;
        this.sessionId = sessionId;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }

    // ========== Getter/Setter 方法 ==========

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "GrpcMessage{" +
                "messageId='" + messageId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", type='" + type + '\'' +
                ", payload='" + payload + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
