package com.network.reactor.netty.collect.client.handler;


import com.network.reactor.netty.collect.domain.PaveRecord;
import com.network.reactor.netty.collect.client.message.impl.ReadRecordRequest;
import com.network.reactor.netty.collect.client.message.impl.ReadRecordResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务器拿到设备信息记录之后，会一直请求客户端获取采集数据。
 * 此handler 就是监听读取采集数据请求，接到请求之后将采集数据上送给netty服务器。
 */
public class ReadRecordResponseHandler extends SimpleChannelInboundHandler<ReadRecordResponse> {


    private static final Logger logger = LoggerFactory.getLogger(ReadRecordResponseHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ReadRecordResponse response) throws Exception {
        logger.info("客户端 => 接收到服务器获取采集数据请。 --> {}", response);
        try {
            List<PaveRecord> list = new ArrayList<>();
            PaveRecord paveRecord = getRecord();
            list.add(paveRecord);

            ReadRecordRequest request = new ReadRecordRequest(list, 3, response.getSequence());
            ctx.writeAndFlush(request);


        } catch (Exception e) {
            logger.info("客户端 => 读取记录消息处理失败", e);
        }
    }



    private PaveRecord getRecord(){
        return new PaveRecord(100L,1,1,1,20.5D,30.5D,23D,33D,45.6D,1D,1D,1D);
    }
}
