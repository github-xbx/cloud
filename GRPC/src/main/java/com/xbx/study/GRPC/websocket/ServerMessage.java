package com.xbx.study.GRPC.websocket;

import org.springframework.web.socket.WebSocketMessage;

public class ServerMessage implements WebSocketMessage {
    @Override
    public Object getPayload() {
        return null;
    }

    @Override
    public int getPayloadLength() {
        return 0;
    }

    @Override
    public boolean isLast() {
        return false;
    }
}
