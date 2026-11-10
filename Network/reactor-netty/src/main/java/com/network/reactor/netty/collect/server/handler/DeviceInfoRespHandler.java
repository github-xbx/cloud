package com.network.reactor.netty.collect.server.handler;

import com.network.reactor.netty.collect.server.message.DeviceInfoRespMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Sharable
public class DeviceInfoRespHandler extends SimpleChannelInboundHandler<DeviceInfoRespMsg> {

    private static final Logger logger =  LogManager.getLogger(DeviceInfoRespHandler.class);


    @Override
    protected void channelRead0(ChannelHandlerContext context, DeviceInfoRespMsg resp) throws Exception {
        logger.info("[Server] => device info req: {}",resp);

        logger.info("[Server] => 保存设备信息 =>TODO");
    }
}
