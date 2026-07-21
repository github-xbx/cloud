package com.xbx.study.ai.event.impl;

import com.xbx.study.ai.enums.AgUiEventType;
import com.xbx.study.ai.event.AgUiEvent;

import java.util.Objects;

public class ReasoningMessageStartEvent extends AgUiEvent {
    private final String messageId;
    private final String role;

    public ReasoningMessageStartEvent(String messageId, String role) {
        super(AgUiEventType.REASONING_MESSAGE_START);
        this.messageId = messageId;
        this.role = role;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReasoningMessageStartEvent that = (ReasoningMessageStartEvent) o;
        return Objects.equals(messageId, that.messageId) && Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, role);
    }
}
