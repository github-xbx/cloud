package com.network.reactor.netty.collect.server.message;

import io.netty.buffer.ByteBuf;

public interface PaveMsgOutput {

    int getSequence();
    int getFrame();

    void encode(ByteBuf buf);
}
