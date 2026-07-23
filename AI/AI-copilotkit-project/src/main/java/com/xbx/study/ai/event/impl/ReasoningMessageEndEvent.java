package com.xbx.study.ai.event.impl;

import com.xbx.study.ai.enums.AgUiEventType;
import com.xbx.study.ai.event.AGUIEvent;

import java.util.Objects;

public class ReasoningMessageEndEvent extends AGUIEvent {
    private final String messageId;

    public ReasoningMessageEndEvent(String messageId) {
        super(AgUiEventType.REASONING_MESSAGE_END);
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReasoningMessageEndEvent that = (ReasoningMessageEndEvent) o;
        return Objects.equals(messageId, that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }
}
