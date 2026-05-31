package com.xbx.study.netty.client.handlers;

import com.xbx.study.netty.protobuf.UserProtobuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientProtobufHandler extends SimpleChannelInboundHandler<UserProtobuf.User> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, UserProtobuf.User user) throws Exception {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //数据的发送

        UserProtobuf.User user = UserProtobuf.User.newBuilder()
                .setUid(1)
                .build();

        ctx.writeAndFlush(user);

    }
}
