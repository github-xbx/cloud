package com.xbx.study.netty.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息过滤处理器
 * 过滤非法消息或黑名单IP
 */
public class MessageFilterHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(MessageFilterHandler.class);

    private final boolean enableFilter;

    /**
     * 默认构造方法，启用过滤
     */
    public MessageFilterHandler() {
        this(true);
    }

    /**
     * 带参数的构造方法
     *
     * @param enableFilter 是否启用过滤
     */
    public MessageFilterHandler(boolean enableFilter) {
        this.enableFilter = enableFilter;
        logger.info("MessageFilterHandler initialized, filter enabled: {}", enableFilter);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String remoteAddress = ctx.channel().remoteAddress().toString();
        logger.info("[Filter] New connection from: {}", remoteAddress);

        // 示例：检查IP黑名单
        if (enableFilter && isBlocked(remoteAddress)) {
            logger.warn("[Filter] Blocked connection from: {}", remoteAddress);
            ctx.close();
            return;
        }

        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (enableFilter) {
            // 示例：过滤非法消息
            if (!isValidMessage(msg)) {
                logger.warn("[Filter] Invalid message filtered: {}", msg);
                return;
            }
        }
        super.channelRead(ctx, msg);
    }

    /**
     * 检查是否在黑名单中（示例实现）
     */
    private boolean isBlocked(String address) {
        // 这里可以实现真正的IP黑名单逻辑
        return false;
    }

    /**
     * 检查消息是否合法（示例实现）
     */
    private boolean isValidMessage(Object msg) {
        // 这里可以实现真正的消息验证逻辑
        return true;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("[Filter] Exception: {}", cause.getMessage(), cause);
        super.exceptionCaught(ctx, cause);
    }
}