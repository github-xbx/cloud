package com.xbx.study.netty.server.handlers;

import com.xbx.study.netty.protobuf.UserProtobuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * protobuf 服务端处理器
 */
public class ServerProtobufHandler extends SimpleChannelInboundHandler<UserProtobuf.User> {

    private static final Logger logger = LoggerFactory.getLogger(ServerProtobufHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, UserProtobuf.User user) throws Exception {

        logger.info("<UNK>");
        logger.info(user.toString());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }
}
