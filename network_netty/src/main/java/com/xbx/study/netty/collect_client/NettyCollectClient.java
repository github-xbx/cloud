package com.xbx.study.netty.collect_client;

import com.xbx.study.netty.collect_client.handler.*;
import com.xbx.study.netty.collect_client.message.impl.DeviceInfoRequest;
import com.xbx.study.netty.collect_client.protocol.MessageCodecSharable;
import com.xbx.study.netty.collect_client.protocol.ProtocolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.net.InetSocketAddress;


public class NettyCollectClient implements DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(NettyCollectClient.class);

    private final InetSocketAddress address;
    private final DeviceInfoRequest deviceInfoRequest;

    private EventLoopGroup eventLoopGroup;
    private Channel channel;

    public NettyCollectClient(InetSocketAddress address, Integer deviceId) {
        this.address = address;
        deviceInfoRequest = new DeviceInfoRequest();
        deviceInfoRequest.setDeviceId(deviceId);
        deviceInfoRequest.setDeviceType(1);
        deviceInfoRequest.setRecodeType(1);
        deviceInfoRequest.setVersion(1);
        deviceInfoRequest.setFrame(2);

        try {
            start();
        } catch (InterruptedException e) {
            throw new RuntimeException("netty 客户端启动失败");
        }

    }

    /**
     * 开启netty 客户端
     * @throws InterruptedException
     */
    public void start() throws InterruptedException {
        eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG)); // 日志
                        ch.pipeline().addLast(new IdleStateHandler(0, 30, 0));
                        ch.pipeline().addLast(new ProtocolFrameDecoder());
                        ch.pipeline().addLast(new MessageCodecSharable() );
                        //ch.pipeline().addLast(new HeartBeatResponseHandler()); //心跳处理
                        ch.pipeline().addLast(new DeviceInfoResponseHandler(deviceInfoRequest)); //处理设备信息读取 handler
                        ch.pipeline().addLast(new ReadRecordResponseHandler()); //处理数据读取 handler
                        ch.pipeline().addLast(new SetWindSpeedUnitHandler());   //处理风速单位设置handler
                        ch.pipeline().addLast(new WriteConfigDataHandler());    //处理写补偿温度
                    }
                });
        ChannelFuture future = bootstrap.connect(address).sync();
        channel = future.channel();
        logger.info("Netty 客户端启动并连接到{}:{}",address.getHostString(), address.getPort());

        // 注册 JVM shutdown hook，确保程序退出时释放 Netty 资源
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            try {
//                destroy();
//            } catch (Exception e) {
//                logger.error("销毁 Netty 客户端失败", e);
//            }
//        }));
    }


    /**
     * 销毁netty 连接资源
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        if (channel != null){
            channel.close().sync();
        }
        if (eventLoopGroup != null){
            eventLoopGroup.shutdownGracefully().sync();
        }
        logger.info("Netty 停止");
    }
}
