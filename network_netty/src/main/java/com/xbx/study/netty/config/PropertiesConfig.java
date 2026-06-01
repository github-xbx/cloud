package com.xbx.study.netty.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropertiesConfig {

    @Value("${server.port}")
    private Integer serverPort;


    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }
    public Integer getServerPort() {
        return serverPort;
    }

}
