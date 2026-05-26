package com.xbx.study.netty.collect_client.handler;

import com.xbx.study.netty.collect_client.message.impl.SetWindSpeedUnitRequest;
import com.xbx.study.netty.collect_client.message.impl.SetWindSpeedUnitResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class SetWindSpeedUnitHandler extends SimpleChannelInboundHandler<SetWindSpeedUnitResponse> {

    private static final Logger logger = LoggerFactory.getLogger(SetWindSpeedUnitHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SetWindSpeedUnitResponse response) throws Exception {
        logger.debug("处理设置风速单位 => {}", response);

        logger.info("设置风速单位(0：km/h； 1：m/s) => {}",response.getUnit());
        SetWindSpeedUnitRequest request = new SetWindSpeedUnitRequest(0);
        request.setFrame(17);

        ctx.writeAndFlush(request).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                logger.debug("客户端 => 发送设备信息请求成功");
            }
        });  //0x00：收到并执行

    }
}
