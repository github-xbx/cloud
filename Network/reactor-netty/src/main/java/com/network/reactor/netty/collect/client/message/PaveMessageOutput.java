package com.network.reactor.netty.collect.client.message;

import io.netty.buffer.ByteBuf;

public interface PaveMessageOutput {
    int getSequence();
    int getFrame();
    void encode(ByteBuf buf);
}
