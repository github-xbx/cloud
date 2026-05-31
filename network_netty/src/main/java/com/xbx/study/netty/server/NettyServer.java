package com.xbx.study.netty.server;

import com.xbx.study.netty.protobuf.UserProtobuf;
import com.xbx.study.netty.server.handlers.ServerProtobufHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;

import java.net.InetSocketAddress;

public class NettyServer {


    private InetSocketAddress address = new  InetSocketAddress("127.0.0.1", 8888);


    public void server(){

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {

                            ChannelPipeline pipeline = socketChannel.pipeline();


                            //取出 Protobuf 消息实例的数据包的包头的包长度=都
                            pipeline.addLast(new ProtobufVarint32FrameDecoder());

                            //protobuf 解码器
                            pipeline.addLast(new ProtobufDecoder(UserProtobuf.User.getDefaultInstance()))
                                    //protobuf 自定以处理器
                                    .addLast(new ServerProtobufHandler());



                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(address).sync();
            channelFuture.channel().closeFuture().sync(); // 阻塞直到服务端关闭
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }



}
