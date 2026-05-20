package com.xbx.study.netty.collect_client.handler;

import com.xbx.study.netty.collect_client.domain.PaveRecord;
import com.xbx.study.netty.collect_client.message.impl.ReadRecordRequest;
import com.xbx.study.netty.collect_client.message.impl.ReadRecordResponse;
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
            //PaveRecord paveRecord = getPaveRecord();
            //list.add(paveRecord);

            ReadRecordRequest request = new ReadRecordRequest(list, 3, response.getSequence());
            ctx.writeAndFlush(request);


        } catch (Exception e) {
            logger.info("客户端 => 读取记录消息处理失败", e);
        }
    }
}
