package com.xbx.study.netty.server.service.impl;

import com.google.protobuf.Message;
import com.xbx.study.netty.protobuf.SyncMessageProto;
import com.xbx.study.netty.server.service.TestService;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl implements TestService {

    private final SyncRequestService  syncRequestService;
    public TestServiceImpl(SyncRequestService syncRequestService) {
        this.syncRequestService = syncRequestService;
    }

    @Override
    public String sendSyncMessage(Integer id) throws Exception {


        //获取netty对应客户端的channel
        Channel channel = new EmbeddedChannel();

        //
        SyncMessageProto.SyncRequest syncRequest = SyncMessageProto.SyncRequest.newBuilder().build();

        Message message = syncRequestService.syncRequest(channel, syncRequest);

        return message.toString();
    }
}
