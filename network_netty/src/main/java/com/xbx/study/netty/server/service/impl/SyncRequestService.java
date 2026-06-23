package com.xbx.study.netty.server.service.impl;

import com.google.protobuf.Message;
import com.xbx.study.netty.service.RequestService;
import io.netty.channel.Channel;
import org.springframework.stereotype.Service;


@Service
public class SyncRequestService implements RequestService {
    @Override
    public String getReqKey(Channel channel, Message req) {

        return channel.id().asLongText();
    }

    @Override
    public String getRespKey(Channel channel, Message resp) {
        return "";
    }
}
