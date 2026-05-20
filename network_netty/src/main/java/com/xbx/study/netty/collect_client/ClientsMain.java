package com.xbx.study.netty.collect_client;

import java.net.InetSocketAddress;

public class ClientsMain {

    public static void main(String[] args) {


        NettyCollectClient client1 = new NettyCollectClient(new InetSocketAddress("127.0.0.1", 46790), 6001);
        NettyCollectClient client2 = new NettyCollectClient(new InetSocketAddress("127.0.0.1", 46790), 6002);

        System.out.println("两个客户端已连接，按 Ctrl+C 退出");

    }








}
