package com.xbx.study.netty.collect_client.handler;

import com.xbx.study.netty.collect_client.message.impl.WriteConfigDataRequest;
import com.xbx.study.netty.collect_client.message.impl.WriteConfigDataResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class WriteConfigDataHandler  extends SimpleChannelInboundHandler<WriteConfigDataResponse> {

    private static final Logger logger = LoggerFactory.getLogger(WriteConfigDataHandler.class);


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WriteConfigDataResponse response) throws Exception {
        logger.debug("处理写补偿温度 => {}", response);

        logger.info("开始补偿温度: {}, 结束补偿温度: {}, 补偿温度: {}", response.getStartTemperature(), response.getEndTemperature(),response.getTemperature());

        WriteConfigDataRequest request = new WriteConfigDataRequest(0);
        ctx.writeAndFlush(request).addListener(future -> {
            if (future.isSuccess()) {
                logger.debug("客户端 => 发送设备信息请求成功");
            }
        });


    }
}
