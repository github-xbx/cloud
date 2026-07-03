package com.xbx.study.device.network.tcp;

import com.xbx.study.device.network.core.NetworkHandler;
import com.xbx.study.device.network.core.message.BaseMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TCP Netty 通道处理器
 * 负责处理 TCP 连接的入站消息，并将消息转发给业务层的 NetworkHandler
 */
public class TcpNettyChannelHandler extends SimpleChannelInboundHandler<BaseMessage> {

    private static final Logger log = LoggerFactory.getLogger(TcpNettyChannelHandler.class);

    private final NetworkHandler<BaseMessage> networkHandler;
    private final TcpSessionManager sessionManager = TcpSessionManager.getInstance();

    public TcpNettyChannelHandler(NetworkHandler<BaseMessage> networkHandler) {
        this.networkHandler = networkHandler;

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String sessionId = channel.id().asShortText();
        
        // 注册会话到管理器
        sessionManager.registerSession(sessionId, channel);
        
        log.info("TCP客户端连接建立: {}, sessionId: {}", channel.remoteAddress(), sessionId);
        super.channelActive(ctx);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BaseMessage msg) throws Exception {
        Channel channel = ctx.channel();
        String sessionId = channel.id().asShortText();
        
        log.info("收到TCP消息: sessionId={}, msg={}", sessionId, msg);

        // 将消息转发给业务层的 NetworkHandler（带 sessionId）
        if (networkHandler != null) {
            networkHandler.onMessage(sessionId, msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String sessionId = channel.id().asShortText();
        
        // 从管理器中注销会话
        sessionManager.unregisterSession(sessionId);
        
        log.info("TCP客户端连接断开: {}, sessionId: {}", channel.remoteAddress(), sessionId);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("TCP连接异常: {}", ctx.channel().remoteAddress(), cause);
        ctx.close();
    }
}
