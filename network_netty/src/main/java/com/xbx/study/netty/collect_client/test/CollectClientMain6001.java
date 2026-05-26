package com.xbx.study.netty.collect_client.test;

import com.xbx.study.netty.collect_client.NettyCollectClient;

import java.net.InetSocketAddress;

public class CollectClientMain6001 {
    public static void main(String[] args) {
        new NettyCollectClient(new InetSocketAddress("127.0.0.1", 46790), 6001);
    }
}
