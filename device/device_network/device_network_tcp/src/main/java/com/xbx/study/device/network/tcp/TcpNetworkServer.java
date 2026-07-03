package com.xbx.study.device.network.tcp;

import com.xbx.study.device.network.core.NetworkHandler;
import com.xbx.study.device.network.core.NetworkServer;
import com.xbx.study.device.network.core.message.BaseMessage;
import com.xbx.study.device.network.core.message.NetworkDownlinkMessage;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TcpNetworkServer implements NetworkServer {

    private static final Logger log = LoggerFactory.getLogger(TcpNetworkServer.class);

    private TcpSessionManager sessionManager = TcpSessionManager.getInstance();

    private int port;

    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Channel serverChannel;
    private volatile boolean running;
    private NetworkHandler<BaseMessage> handler;

    public TcpNetworkServer() {
    }

    public TcpNetworkServer(int port, NetworkHandler<BaseMessage> handler) {
        this.port = port;
        this.handler = handler;
    }


    @Override
    public void start() throws Throwable {
        log.info("启动TCP服务器，端口: {}", port);
        ChannelFuture server = server();
        running = true;
        log.info("TCP服务器启动成功，端口: {}", port);
    }

    @Override
    public void stop() throws Throwable {
        log.info("停止TCP服务器...");
        running = false;
        destroy();
        log.info("TCP服务器已停止");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void send(NetworkDownlinkMessage message) {
        if (message == null) {
            log.warn("发送消息失败：消息为空");
            return;
        }

        String sessionId = message.getSessionId();
        String deviceId = message.getDeviceId();

        // 优先使用 sessionId
        if (sessionId != null) {
            sendToSession(sessionId, message.getPayload());
        } 
        // 其次使用 deviceId
        else if (deviceId != null) {
            sendToDevice(deviceId, message.getPayload());
        } 
        else {
            log.warn("发送消息失败：sessionId和deviceId都为空");
        }
    }

    /**
     * 向指定会话发送消息
     * 
     * @param sessionId 会话ID
     * @param message 消息内容
     */
    public void sendToSession(String sessionId, String message) {
        if (sessionId == null || message == null) {
            log.warn("发送消息失败：sessionId或消息为空");
            return;
        }

        Channel channel = sessionManager.getChannel(sessionId);
        if (channel == null || !channel.isActive()) {
            log.warn("发送消息失败：找不到活动的连接, sessionId={}", sessionId);
            return;
        }

        log.info("发送TCP消息: sessionId={}, message={}", sessionId, message);
        ByteBuf byteBuf = Unpooled.copiedBuffer(message + "\n", StandardCharsets.UTF_8);
        channel.writeAndFlush(byteBuf).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.debug("消息发送成功: sessionId={}", sessionId);
            } else {
                log.error("消息发送失败: sessionId={}", sessionId, future.cause());
            }
        });
    }

    /**
     * 向指定设备发送消息
     * 
     * @param deviceId 设备ID
     * @param message 消息内容
     */
    public void sendToDevice(String deviceId, String message) {
        if (deviceId == null || message == null) {
            log.warn("发送消息失败：deviceId或消息为空");
            return;
        }

        if (!sessionManager.isDeviceOnline(deviceId)) {
            log.warn("发送消息失败：设备不在线, deviceId={}", deviceId);
            return;
        }

        // 获取设备的所有会话（通常只有一个）
        Set<String> sessionIds = sessionManager.getSessionIds(deviceId);
        if (sessionIds == null || sessionIds.isEmpty()) {
            log.warn("发送消息失败：找不到设备的会话, deviceId={}", deviceId);
            return;
        }

        // 向设备的所有会话发送消息（支持设备多连接场景）
        for (String sessionId : sessionIds) {
            sendToSession(sessionId, message);
        }
    }

    /**
     * 向所有设备广播消息
     * 
     * @param message 消息内容
     */
    public void broadcast(String message) {
        if (message == null) {
            log.warn("广播消息失败：消息为空");
            return;
        }

        int deviceCount = sessionManager.getOnlineDeviceCount();
        if (deviceCount == 0) {
            log.warn("广播消息失败：没有在线设备");
            return;
        }

        log.info("向 {} 个设备广播消息: {}", deviceCount, message);
        ByteBuf byteBuf = Unpooled.copiedBuffer(message + "\n", StandardCharsets.UTF_8);
        
        sessionManager.getActiveSessionIds().forEach(sessionId -> {
            Channel channel = sessionManager.getChannel(sessionId);
            if (channel != null && channel.isActive()) {
                channel.writeAndFlush(byteBuf.retainedDuplicate()).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.debug("广播消息成功: sessionId={}", sessionId);
                    } else {
                        log.error("广播消息失败: sessionId={}", sessionId, future.cause());
                    }
                });
            }
        });
        
        byteBuf.release();
    }


    /**
     * 获取所有在线设备ID
     * 
     * @return 设备ID列表
     */
    public List<String> getOnlineDeviceIds() {
        return new ArrayList<>(sessionManager.getOnlineDeviceIds());
    }

    /**
     * 获取所有活动会话ID
     * 
     * @return 会话ID列表
     */
    public List<String> getActiveSessionIds() {
        return new ArrayList<>(sessionManager.getActiveSessionIds());
    }

    /**
     * 检查设备是否在线
     * 
     * @param deviceId 设备ID
     * @return 是否在线
     */
    public boolean isDeviceOnline(String deviceId) {
        return sessionManager.isDeviceOnline(deviceId);
    }

    /**
     * 检查会话是否活跃
     * 
     * @param sessionId 会话ID
     * @return 是否活跃
     */
    public boolean isSessionActive(String sessionId) {
        return sessionManager.isSessionActive(sessionId);
    }

    /**
     * 获取在线设备数量
     * 
     * @return 设备数量
     */
    public int getOnlineDeviceCount() {
        return sessionManager.getOnlineDeviceCount();
    }

    /**
     * 获取活动会话数量
     * 
     * @return 会话数量
     */
    public int getActiveSessionCount() {
        return sessionManager.getActiveSessionCount();
    }

    /**
     * 断开指定设备的所有连接
     * 
     * @param deviceId 设备ID
     */
    public void disconnectDevice(String deviceId) {
        Set<String> sessionIds = sessionManager.getSessionIds(deviceId);
        if (sessionIds != null) {
            sessionIds.forEach(sessionId -> {
                Channel channel = sessionManager.getChannel(sessionId);
                if (channel != null && channel.isActive()) {
                    log.info("断开设备连接: deviceId={}, sessionId={}", deviceId, sessionId);
                    channel.close();
                }
            });
        }
    }

    /**
     * 断开指定会话的连接
     * 
     * @param sessionId 会话ID
     */
    public void disconnectSession(String sessionId) {
        Channel channel = sessionManager.getChannel(sessionId);
        if (channel != null && channel.isActive()) {
            log.info("断开会话连接: sessionId={}", sessionId);
            channel.close();
        }
    }

    /**
     * 启动 Netty 服务器
     */
    public ChannelFuture server() {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();

                            // 添加基于换行符的帧解码器（最大帧长度 1024 字节）
                            pipeline.addLast("frameDecoder", new DelimiterBasedFrameDecoder(1024, 
                                    Delimiters.lineDelimiter()));
                            
                            // 字符串解码器
                            pipeline.addLast("stringDecoder", new StringDecoder(StandardCharsets.UTF_8));
                            
                            // 字符串编码器
                            pipeline.addLast("stringEncoder", new StringEncoder(StandardCharsets.UTF_8));

                            // 业务处理器
                            pipeline.addLast("businessHandler", new TcpNettyChannelHandler(handler));
                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            this.serverChannel = channelFuture.channel();

            // 监听服务器关闭
            this.serverChannel.closeFuture().addListener((ChannelFutureListener) future -> {
                log.info("TCP服务器通道已关闭");
                destroy();
            });

            return channelFuture;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("启动TCP服务器被中断", e);
        }
    }

    /**
     * 优雅关闭 Netty 服务器
     */
    public void destroy() {
        // 关闭所有活动的客户端连接
        sessionManager.getActiveSessionIds().forEach(sessionId -> {
            Channel channel = sessionManager.getChannel(sessionId);
            if (channel != null && channel.isActive()) {
                channel.close();
            }
        });

        // 关闭服务器通道
        if (serverChannel != null && serverChannel.isActive()) {
            serverChannel.close();
        }

        // 优雅关闭 EventLoopGroup
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
