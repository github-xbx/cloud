package com.network.reactor.netty.collect.server.handler.reactive.handler;



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
        logger.info("[Server] 采集记录 => readCount={}, noConfirm={}, records={}", msg.getReadCount(), msg.getNoConfirm(), msg.getRecords().size());

        if (msg.getNoConfirm() != null && msg.getNoConfirm() > 0) {
            return Mono.just(new ReadRecordReqMsg(0, 0, 1));
        }
        return Mono.empty();
    }
}
