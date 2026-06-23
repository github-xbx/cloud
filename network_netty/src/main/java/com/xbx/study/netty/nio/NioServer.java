package com.xbx.study.netty.nio;

import java.nio.ByteBuffer;

public class NioServer {


    /**
     * java nio 核心三大组件
     *
     *   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
     *   │  Channel    │    │  Buffer     │    │  Selector   │
     *   │  (通道)      │◄──►│  (缓冲区)    │    │  (多路复用器) │
     *   │  双向传输    │    │  数据容器     │    │  事件监听     │
     *   └─────────────┘    └─────────────┘    └─────────────┘
     *
     */
    public static void main(String[] args) {

        //创建缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);  //堆内存缓冲区



    }


}
