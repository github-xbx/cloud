package com.network.reactor.netty.collect.server.handler;

import com.network.reactor.netty.collect.server.message.SetWindSpeedUnitRespMsg;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Sharable
public class SetWindSpeedUnitRespHandler extends SimpleChannelInboundHandler<SetWindSpeedUnitRespMsg> {

    private static final Logger logger = LogManager.getLogger(SetWindSpeedUnitRespHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext context, SetWindSpeedUnitRespMsg message) throws Exception {

        logger.info("[Server] => setting wind speed unit success； result => {}",message);




    }
}
