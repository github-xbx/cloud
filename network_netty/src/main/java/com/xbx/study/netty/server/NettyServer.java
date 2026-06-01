package com.xbx.study.netty.server;

import com.xbx.study.common.constants.NettySceneEnum;
import com.xbx.study.netty.server.handlers.ServerHandlers;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private InetSocketAddress address;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private Channel channel;
    private NettySceneEnum scene;

    public NettyServer() {
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
    }

    public NettyServer(InetSocketAddress address, NettySceneEnum scene) {
        this.address = address;
        this.scene = scene;
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
    }

    /**
     * 启动 Netty 服务器
     *
     * @return ChannelFuture
     */
    public ChannelFuture server() {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();

                            // 根据配置动态加载handler

                            List<ChannelHandler> handlers = ServerHandlers.builder().scene(scene).build().getHandlerList();
                            for (ChannelHandler handler : handlers) {
                                pipeline.addLast(handler);
                                logger.debug("Added handler: {}", handler.getClass().getSimpleName());
                            }
                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(address).sync();
            this.channel = channelFuture.channel();
            logger.debug("Netty server started successfully on {}", address);
            return channelFuture;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 优雅关闭 Netty 服务器
     */
    public void destroy() {
        if (channel != null) {
            channel.close();
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        logger.info("Netty server stopped");
    }

}