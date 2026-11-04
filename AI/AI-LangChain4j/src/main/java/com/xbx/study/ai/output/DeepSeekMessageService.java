package com.xbx.study.ai.output;

import com.xbx.study.ai.service.Chat;
import com.xbx.study.ai.service.StreamChatService;
import org.springframework.stereotype.Component;

@Component
public class DeepSeekMessageService implements AiMessageService{


    @Override
    public StreamChatService streamService() {
        return null;
    }

    @Override
    public Chat chatService() {
        return null;
    }
}
