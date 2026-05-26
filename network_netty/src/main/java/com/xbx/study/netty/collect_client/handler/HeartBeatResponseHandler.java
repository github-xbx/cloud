package com.xbx.study.netty.collect_client.handler;

import com.xbx.study.netty.collect_client.message.impl.HeartBeatRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * new IdleStateHandler(0, 30, 0) 30秒内没有写数据，发送一个心跳包给服务端
 * 当如果 30 秒内没有向对端写出任何数据，就会触发一次 WRITER_IDLE 事件。
 * 该监听器会监听 WRITER_IDLE 事件，如果触发了 WRITER_IDLE 事件，就会发送一个心跳包给服务端。
 */
public class HeartBeatResponseHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HeartBeatResponseHandler.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE){
                // 30秒未写数据，发送一个心跳包给服务端
                HeartBeatRequest request = new HeartBeatRequest();
                ctx.writeAndFlush(request);
                logger.debug("客户端 => 发送心跳包 {}", request);
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }

    }
}
