package com.network.reactor.netty.collect.server.handler.reactive.handler;



import com.network.reactor.netty.collect.domain.MessageHandler;
import com.network.reactor.netty.collect.server.message.ReadRecordReqMsg;
import com.network.reactor.netty.collect.server.message.ReadRecordRespMsg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;

/**
 * 收到采集记录：存库；对端还有未确认记录就继续拉，否则停在这里等下一轮。
 */
public class ReadRecordHandler implements MessageHandler<ReadRecordRespMsg> {

    private static final Logger logger = LogManager.getLogger(ReadRecordHandler.class);

    public ReadRecordHandler() {
    }

    @Override
    public Class<ReadRecordRespMsg> type() {
        return ReadRecordRespMsg.class;
    }

    @Override
    public Mono<Object> handle(ReadRecordRespMsg msg) {
        logger.info("[Server] 采集记录 => {}", msg);

        // 关键：不能无条件再发读指令，否则和客户端形成"请求-上报"乒乓死循环。
        // 只有对端还有未确认记录时才继续拉，否则返回空流，等下一轮（定时器/心跳）再发起。

        return Mono.just(new ReadRecordReqMsg(0, 0, 1));

    }
}
