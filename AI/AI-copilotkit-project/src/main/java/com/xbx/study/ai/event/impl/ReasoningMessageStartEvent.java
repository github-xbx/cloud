package com.xbx.study.ai.event.impl;

import com.xbx.study.ai.enums.AgUiEventType;
import com.xbx.study.ai.event.AgUiEvent;

import java.util.Objects;

public class ReasoningMessageStartEvent extends AgUiEvent {
    private final String messageId;

    public ReasoningMessageStartEvent(String messageId) {
        super(AgUiEventType.REASONING_MESSAGE_START);
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReasoningMessageStartEvent that = (ReasoningMessageStartEvent) o;
        return Objects.equals(messageId, that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }
}
