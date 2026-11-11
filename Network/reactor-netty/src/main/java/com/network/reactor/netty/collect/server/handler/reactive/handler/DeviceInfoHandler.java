package com.network.reactor.netty.collect.server.handler.reactive.handler;

import com.network.reactor.netty.collect.server.message.DeviceInfoRespMsg;
import com.network.reactor.netty.collect.server.message.ReadRecordReqMsg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;

/**
 * 收到设备信息：存库，然后紧接着下发「读记录」。
 * 状态迁移（WAIT_DEVICE_INFO -> WAIT_RECORDS）由 MessageRouter 的注册处声明，这里不写。
 */
public class DeviceInfoHandler implements MessageHandler<DeviceInfoRespMsg> {

    private static final Logger logger = LogManager.getLogger(DeviceInfoHandler.class);

    public DeviceInfoHandler() {}

    @Override
    public Class<DeviceInfoRespMsg> type() {
        return DeviceInfoRespMsg.class;
    }

    @Override
    public Mono<Object> handle(DeviceInfoRespMsg msg) {
        logger.info("[Server] 设备信息 => {}", msg);
        return Mono.just(new ReadRecordReqMsg(0, 0, 1));
    }
}
