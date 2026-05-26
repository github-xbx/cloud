package com.xbx.study.netty.collect_client.message;

import io.netty.buffer.ByteBuf;

public interface PaveMessageInput {
    void setSequence(int seq);
    void setFrame(int frame);
    void setCrcValid(boolean crcValid);
    void decode(ByteBuf buf);
}
