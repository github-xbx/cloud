package com.xbx.study.netty.collect_client.message;

import io.netty.buffer.ByteBuf;

public interface PaveMessageOutput {
    int getSequence();
    int getFrame();
    void encode(ByteBuf buf);
}
