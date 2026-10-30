package com.xbx.study.ai.output;


import com.xbx.study.ai.service.Chat;
import com.xbx.study.ai.service.StreamChat;


public interface AiMessageService {

    StreamChat streamService();

    Chat chatService();



}
