package com.xbx.study.ai.listener;

import com.xbx.study.ai.entity.po.AiMessagePo;
import com.xbx.study.ai.mapper.AiMessageMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.invocation.InvocationParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.observability.api.event.AiServiceCompletedEvent;
import dev.langchain4j.observability.api.listener.AiServiceCompletedListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 保存回答信息 listener
 */
@Component
public class ResponseSaveListener implements AiServiceCompletedListener {

    private final AiMessageMapper aiMessageMapper;

    public ResponseSaveListener(AiMessageMapper aiMessageMapper) {
        this.aiMessageMapper = aiMessageMapper;

    }

    @Override
    public void onEvent(AiServiceCompletedEvent event) {
        ChatResponse response = (ChatResponse) event.result().get();
        AiMessage aiMessage = response.aiMessage();
        String text = aiMessage.text();
        String thinking = aiMessage.thinking();
        InvocationParameters parameters = event.invocationContext().invocationParameters();

        AiMessagePo thinkMessage = new AiMessagePo(parameters.get("thinkingId"), parameters.get("threadId"), parameters.get("runId"), "reasoning", thinking, LocalDateTime.now());
        AiMessagePo textMessage = new AiMessagePo(parameters.get("textId"), parameters.get("threadId"), parameters.get("runId"), "assistant", text, LocalDateTime.now());

        aiMessageMapper.batchInsert(List.of(thinkMessage, textMessage));

    }
}
