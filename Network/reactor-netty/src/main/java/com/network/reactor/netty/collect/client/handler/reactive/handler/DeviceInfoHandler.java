package com.network.reactor.netty.collect.client.handler.reactive.handler;

import com.network.reactor.netty.collect.client.message.impl.DeviceInfoRequest;
import com.network.reactor.netty.collect.client.message.impl.DeviceInfoResponse;
import com.network.reactor.netty.collect.domain.MessageHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

public class DeviceInfoHandler implements MessageHandler<DeviceInfoResponse> {

    private static final Logger logger = LogManager.getLogger(DeviceInfoHandler.class);

    @Override
    public Class<DeviceInfoResponse> type() {
        return DeviceInfoResponse.class;
    }

    @Override
    public Mono<Object> handle(DeviceInfoResponse msg) {

        logger.debug("客户端 => 处理服务端 读取设备信息 请求");
        try {
            DeviceInfoRequest deviceInfoRequest = new DeviceInfoRequest();
            deviceInfoRequest.setDeviceId(100);
            deviceInfoRequest.setDeviceType(1);
            deviceInfoRequest.setRecodeType(1);
            deviceInfoRequest.setVersion(1);
            deviceInfoRequest.setFrame(2);

            TimeUnit.SECONDS.sleep(2);
            return Mono.just(deviceInfoRequest);
        }catch (Exception e) {
            logger.error("客户端 => 设备响应消息处理失败", e);

        }
        return Mono.empty();
    }
}
