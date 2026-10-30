package com.xbx.study.ai.service;

public interface StreamChat extends BaseAiChat{


    default String type() {
        return "STREAM_CHAT";
    }


}
