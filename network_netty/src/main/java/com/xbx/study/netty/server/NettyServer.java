package com.xbx.study.netty.server;

import com.xbx.study.netty.protobuf.UserProtobuf;
import com.xbx.study.netty.server.handlers.ServerChannelEventHandler;
import com.xbx.study.netty.server.handlers.ServerProtobufHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private InetSocketAddress address = new  InetSocketAddress("127.0.0.1", 8000);

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private Channel channel;

    public NettyServer(){
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
    }
    public NettyServer(InetSocketAddress address){
        this.address = address;
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
    }


    /**
     * 启动 netty 服务器
     * @return ChannelFuture
     */
    public ChannelFuture server(){
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();

                            // 心跳检测：30秒未收到读操作则触发 READER_IDLE 事件
                            //pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));

                            // Channel 事件监听器：连接、断开、异常、心跳超时
                            pipeline.addLast(new ServerChannelEventHandler());

                            //取出 Protobuf 消息实例的数据包的包头的包长度
                            pipeline.addLast(new ProtobufVarint32FrameDecoder());

                            //protobuf 解码器
                            pipeline.addLast(new ProtobufDecoder(UserProtobuf.User.getDefaultInstance()))
                                    //protobuf 自定义处理器
                                    .addLast(new ServerProtobufHandler());
                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(address).sync();
            this.channel = channelFuture.channel();
            logger.debug("netty server 启动成功。。。。");
            return channelFuture;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



    /**
     * 优雅关闭 Netty 服务器
     * 1. 关闭 Channel（停止接收新连接）
     * 2. 优雅关闭 EventLoopGroup（等待正在处理的任务完成）
     */
    public void destroy() {
        if (channel != null) {
            channel.close(); // 关闭 server channel，停止接收新连接
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully(); // 优雅关闭 boss 线程组
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully(); // 优雅关闭 worker 线程组
        }
        logger.debug("netty server 关闭。。。。");
    }




}
