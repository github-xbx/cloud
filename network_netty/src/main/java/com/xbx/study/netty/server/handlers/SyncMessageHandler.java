package com.xbx.study.netty.server.handlers;

import com.google.protobuf.Message;
import com.xbx.study.netty.protobuf.SyncMessageProto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 同步消息处理器
 */
public class SyncMessageHandler extends SimpleChannelInboundHandler<SyncMessageProto.SyncResponse> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SyncMessageProto.SyncResponse msg) throws Exception {

    }
}