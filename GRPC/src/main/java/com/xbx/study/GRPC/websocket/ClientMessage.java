package com.xbx.study.GRPC.websocket;

import java.io.Serializable;

public class ClientMessage implements Serializable {



    private String type;

    private String text;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "ClientMessage{" +
                "type='" + type + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
