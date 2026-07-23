package com.xbx.study.ai.event.impl;

import com.xbx.study.ai.enums.AgUiEventType;
import com.xbx.study.ai.event.AGUIEvent;

import java.util.Objects;

public class TextMessageStartEvent extends AGUIEvent {
    private final String messageId;
    private final String role; // "user" | "assistant"

    public TextMessageStartEvent(String messageId, String role) {
        super(AgUiEventType.TEXT_MESSAGE_START);
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
        TextMessageStartEvent that = (TextMessageStartEvent) o;
        return Objects.equals(messageId, that.messageId) && Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, role);
    }
}
