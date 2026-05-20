package com.xbx.study.netty.collect_client.handler;

import com.xbx.study.netty.collect_client.message.impl.DeviceInfoRequest;
import com.xbx.study.netty.collect_client.message.impl.DeviceInfoResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 建立连接成功后 服务端会主动发送消息 查询设备信息
 * 客户端以此handler处理 并返回设备信息给服务端
 *
 */
@ChannelHandler.Sharable
public class DeviceInfoResponseHandler extends SimpleChannelInboundHandler<DeviceInfoResponse> {

    private static final Logger logger = LoggerFactory.getLogger(DeviceInfoResponseHandler.class);

    private DeviceInfoRequest deviceInfoRequest;

    public DeviceInfoResponseHandler(DeviceInfoRequest deviceInfoRequest) {
        this.deviceInfoRequest = deviceInfoRequest;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DeviceInfoResponse deviceInfoResponse) throws Exception {
        logger.info("客户端 => 处理服务端 读取设备信息 请求");
        try {
            logger.info("send message body {}", deviceInfoRequest);
            //发送的消息不在handler 中定义，由客户端定义
            ctx.writeAndFlush(deviceInfoRequest).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    logger.info("客户端 => 发送设备信息请求成功");
                }
            });
        }catch (Exception e) {
            logger.info("客户端 => 设备响应消息处理失败", e);
        }
    }
}
