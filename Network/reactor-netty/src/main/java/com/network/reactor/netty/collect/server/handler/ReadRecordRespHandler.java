package com.network.reactor.netty.collect.server.handler;

import com.network.reactor.netty.collect.server.message.DeviceInfoReqMsg;
import com.network.reactor.netty.collect.server.message.ReadRecordRespMsg;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Sharable
public class ReadRecordRespHandler extends SimpleChannelInboundHandler<ReadRecordRespMsg> {
    private static final Logger logger =  LogManager.getLogger(ReadRecordRespHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext context, ReadRecordRespMsg msg) throws Exception {
        logger.info("[Server] => data: {}",msg);

        logger.info("[Server] => 保存 数据 TODO");

        //发送给客户端继续请求数据
        context.writeAndFlush(new DeviceInfoReqMsg());

    }
}
