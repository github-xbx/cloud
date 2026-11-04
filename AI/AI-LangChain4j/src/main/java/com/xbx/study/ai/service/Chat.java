package com.xbx.study.ai.service;

public interface Chat extends BaseAiChat {

    default String type(){
        return "CHAT";
    }
}
