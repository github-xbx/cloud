package com.xbx.study.netty.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * 服务端 Channel 事件监听器
 * 监听客户端的连接、断开、异常、心跳超时等事件
 */
public class ServerChannelEventHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ServerChannelEventHandler.class);

    /**
     * 客户端连接建立（channel 激活）
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        logger.info("客户端连接: {}:{}，ChannelId: {}",
                address.getHostString(), address.getPort(), ctx.channel().id().asShortText());
        super.channelActive(ctx);
    }

    /**
     * 客户端断开连接（channel 失活）
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        logger.info("客户端断开: {}:{}，ChannelId: {}",
                address.getHostString(), address.getPort(), ctx.channel().id().asShortText());
        super.channelInactive(ctx);
    }

    /**
     * channel 注册到 EventLoop
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Channel 注册: {}", ctx.channel().id().asShortText());
        super.channelRegistered(ctx);
    }

    /**
     * channel 从 EventLoop 注销
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Channel 注销: {}", ctx.channel().id().asShortText());
        super.channelUnregistered(ctx);
    }

    /**
     * 异常捕获
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        logger.error("客户端异常: {}:{}，原因: {}", address.getHostString(), address.getPort(), cause.getMessage(), cause);
        ctx.close(); // 发生异常时关闭连接
    }

    /**
     * 自定义事件触发（如心跳超时）
     * 需要在 pipeline 中添加 IdleStateHandler 才能收到 IdleStateEvent
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                logger.warn("读空闲超时，关闭连接: {}", ctx.channel().id().asShortText());
                ctx.close();
            } else if (event.state() == IdleState.WRITER_IDLE) {
                logger.warn("写空闲超时: {}", ctx.channel().id().asShortText());
            } else if (event.state() == IdleState.ALL_IDLE) {
                logger.warn("读写空闲超时，关闭连接: {}", ctx.channel().id().asShortText());
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
