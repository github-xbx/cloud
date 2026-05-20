package com.xbx.study.netty.collect_client.handler;

import com.xbx.study.netty.collect_client.message.impl.HeartBeatRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class HeartBeatResponseHandler extends SimpleChannelInboundHandler<HeartBeatRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HeartBeatRequest heartBeatRequest) throws Exception {

    }
}
