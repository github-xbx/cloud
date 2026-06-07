package com.xbx.study.netty.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 心跳检测处理器
 * 定期发送心跳消息，检测连接活性
 */
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatHandler.class);

    private final long heartbeatInterval;

    /**
     * 默认构造方法，使用默认心跳间隔30秒
     */
    public HeartbeatHandler() {
        this(30);
    }

    /**
     * 带参数的构造方法
     *
     * @param heartbeatInterval 心跳间隔（秒）
     */
    public HeartbeatHandler(long heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
        logger.info("HeartbeatHandler initialized with interval: {}s", heartbeatInterval);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("[Heartbeat] Channel active, starting heartbeat for: {}", ctx.channel().remoteAddress());
        // 这里可以启动心跳定时任务
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("[Heartbeat] Channel inactive, stopping heartbeat for: {}", ctx.channel().remoteAddress());
        // 这里可以停止心跳定时任务
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("[Heartbeat] Received message: {}", msg);
        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("[Heartbeat] Exception: {}", cause.getMessage(), cause);
        super.exceptionCaught(ctx, cause);
    }

    public long getHeartbeatInterval() {
        return heartbeatInterval;
    }
}