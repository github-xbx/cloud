package com.network.reactor.netty.collect.client.handler.reactive.handler;

import com.network.reactor.netty.collect.client.message.impl.ReadRecordRequest;
import com.network.reactor.netty.collect.client.message.impl.ReadRecordResponse;
import com.network.reactor.netty.collect.domain.MessageHandler;
import com.network.reactor.netty.collect.domain.PaveRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReadRecordHandler implements MessageHandler<ReadRecordResponse> {

    private static final Logger logger = LogManager.getLogger(ReadRecordHandler.class);

    @Override
    public Class<ReadRecordResponse> type() {
        return ReadRecordResponse.class;
    }

    @Override
    public Mono<Object> handle(ReadRecordResponse response) {
        logger.info("客户端 => 接收到服务器获取采集数据请。 --> {}", response);
        try {
            List<PaveRecord> list = new ArrayList<>();
            PaveRecord paveRecord = getRecord();
            list.add(paveRecord);

            ReadRecordRequest request = new ReadRecordRequest(list, 3, response.getSequence());
            TimeUnit.SECONDS.sleep(5);
            return Mono.just(request);


        } catch (Exception e) {
            logger.info("客户端 => 读取记录消息处理失败", e);
        }

        return Mono.empty();
    }

    private PaveRecord getRecord(){
        return new PaveRecord(100L,1,1,1,20.5D,30.5D,23D,33D,45.6D,1D,1D,1D);
    }

}
