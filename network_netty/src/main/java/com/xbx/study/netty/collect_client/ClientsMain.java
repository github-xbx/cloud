package com.xbx.study.netty.collect_client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class ClientsMain {

    private static final Logger logger = LoggerFactory.getLogger(ClientsMain.class);

    public static void main(String[] args) {

        logger.debug("客户端Main开始执行");


        NettyCollectClient client1 = new NettyCollectClient(new InetSocketAddress("127.0.0.1", 46790), 6001);
        NettyCollectClient client2 = new NettyCollectClient(new InetSocketAddress("127.0.0.1", 46790), 6002);

        System.out.println("两个客户端已连接，按 Ctrl+C 退出");

    }








}
