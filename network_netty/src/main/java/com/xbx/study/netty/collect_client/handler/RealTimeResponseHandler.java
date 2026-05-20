package com.xbx.study.netty.collect_client.handler;

import com.xbx.study.netty.collect_client.message.impl.RealTimeResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务器向客户端读取实时数据，客户端处理器
 */
public class RealTimeResponseHandler extends SimpleChannelInboundHandler<RealTimeResponse> {

    private static final Logger logger = LoggerFactory.getLogger(RealTimeResponseHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RealTimeResponse response) throws Exception {
        logger.debug("RealTimeRespMsg => {}", response);
        try {
            //ContextHolder.getBean(PaveService.class).update(ctx.channel(), msg);
        } catch (Exception e) {
            logger.error("实时数据消息处理失败", e);
        }
    }
}
