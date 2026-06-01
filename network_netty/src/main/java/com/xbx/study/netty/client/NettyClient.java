package com.xbx.study.netty.client;

import com.xbx.study.netty.client.handlers.ClientProtobufHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class NettyClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private InetSocketAddress address = new InetSocketAddress("127.0.0.1",8000);
    private Channel channel;
    private NioEventLoopGroup group = new NioEventLoopGroup();


    public void client(){
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            // 发送：加长度前缀 + 编码
                            pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                            pipeline.addLast(new ProtobufEncoder());

                            pipeline.addLast(new ClientProtobufHandler());
                        }
                    });
            ChannelFuture future = bootstrap.connect(address).sync();
            channel = future.channel();
            logger.info("Netty 客户端启动并连接到{}:{}",address.getHostString(), address.getPort());

            // 注册 JVM shutdown hook，确保程序退出时释放 Netty 资源
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    destroy();
                } catch (Exception e) {
                    logger.error("销毁 Netty 客户端失败", e);
                }
            }));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


    public void destroy() throws Exception {
        if (channel != null){
            channel.close().sync();
        }
        if (group != null){
            group.shutdownGracefully().sync();
        }
        logger.info("Netty 停止");
    }

}
