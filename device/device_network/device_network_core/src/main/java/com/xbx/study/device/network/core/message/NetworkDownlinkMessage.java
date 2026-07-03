package com.xbx.study.device.network.core.message;

import com.xbx.study.device.network.core.enums.NetworkProtocol;

public class NetworkDownlinkMessage {

    private String bindingId;
    private String gatewayNodeId;
    private String deviceId;
    private String deviceCode;
    private String sessionId;
    private NetworkProtocol protocol;
    private String topic;
    private String payload;


    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    public String getGatewayNodeId() {
        return gatewayNodeId;
    }

    public void setGatewayNodeId(String gatewayNodeId) {
        this.gatewayNodeId = gatewayNodeId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public NetworkProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(NetworkProtocol protocol) {
        this.protocol = protocol;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
