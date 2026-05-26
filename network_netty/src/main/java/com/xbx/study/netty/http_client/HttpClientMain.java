package com.xbx.study.netty.http_client;

import java.net.InetSocketAddress;

public class HttpClientMain {


    public static void main(String[] args) {

        String body = "{\"deviceId\":6001,\"unit\":1}";

        new NettyHttpClient(new InetSocketAddress("127.0.0.1",46791))
                .sendPost("/pave/settingWindSpeedUnit",body);

    }
}
