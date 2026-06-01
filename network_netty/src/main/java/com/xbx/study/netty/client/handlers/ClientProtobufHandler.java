package com.xbx.study.netty.client.handlers;

import com.xbx.study.netty.protobuf.UserProtobuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientProtobufHandler extends SimpleChannelInboundHandler<UserProtobuf.User> {

    private static final Logger logger = LoggerFactory.getLogger(ClientProtobufHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, UserProtobuf.User user) throws Exception {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //数据的发送

        UserProtobuf.User user = UserProtobuf.User.newBuilder()
                .setUid(1)
                .build();
        logger.info("客户端发送消息，{}",user.toString());
        ctx.writeAndFlush(user);

    }
}
