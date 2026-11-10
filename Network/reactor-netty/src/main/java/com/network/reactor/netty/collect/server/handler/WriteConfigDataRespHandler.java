package com.network.reactor.netty.collect.server.handler;

import com.network.reactor.netty.collect.server.message.WriteConfigDataRespMsg;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Sharable
public class WriteConfigDataRespHandler extends SimpleChannelInboundHandler<WriteConfigDataRespMsg> {

    private static final Logger logger = LogManager.getLogger(WriteConfigDataRespHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext context, WriteConfigDataRespMsg message) throws Exception {
        logger.info("[Server] => write configuration data success； result => {}",message);
    }
}
