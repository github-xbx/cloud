package com.xbx.study.ai.event.impl;

import com.xbx.study.ai.enums.AgUiEventType;
import com.xbx.study.ai.event.AGUIEvent;

import java.util.Objects;


public class ReasoningMessageContentEvent extends AGUIEvent {
    private final String messageId;
    private final String delta;

    public ReasoningMessageContentEvent(String messageId, String delta) {
        super(AgUiEventType.REASONING_MESSAGE_CONTENT);
        this.messageId = messageId;
        this.delta = delta;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getDelta() {
        return delta;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReasoningMessageContentEvent that = (ReasoningMessageContentEvent) o;
        return Objects.equals(messageId, that.messageId) && Objects.equals(delta, that.delta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, delta);
    }
}
