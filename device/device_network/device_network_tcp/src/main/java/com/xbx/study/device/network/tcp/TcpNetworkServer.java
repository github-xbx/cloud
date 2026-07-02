package com.xbx.study.device.network.tcp;

import com.xbx.study.device.network.core.NetworkHandler;
import com.xbx.study.device.network.core.NetworkServer;
import com.xbx.study.device.network.core.message.NetworkDownlinkMessage;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;



public class TcpNetworkServer implements NetworkServer {

    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Channel channel;
    private volatile boolean running;
    private final int prot;

    public TcpNetworkServer(int prot, NetworkHandler<?> handler) {
        this.prot = prot;
    }


    @Override
    public void start() throws Throwable {
        ChannelFuture server = server();
        running = true;
    }

    @Override
    public void stop() throws Throwable {

        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void send(NetworkDownlinkMessage message) {

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

                            pipeline.addLast(new ChannelInboundHandlerAdapter());


                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(prot).sync();
            this.channel = channelFuture.channel();

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
    }


}
