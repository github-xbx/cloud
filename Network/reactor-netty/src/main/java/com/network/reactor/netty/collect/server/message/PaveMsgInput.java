package com.network.reactor.netty.collect.server.message;

import io.netty.buffer.ByteBuf;

public interface PaveMsgInput {
    void setSequence(int seq);
    void setFrame(int frame);
    void setCrcValid(boolean crcValid);

    void decode(ByteBuf buf);
}
