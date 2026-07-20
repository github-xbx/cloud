package com.xbx.study.ai.event.impl;

import com.xbx.study.ai.enums.AgUiEventType;
import com.xbx.study.ai.event.AgUiEvent;

import java.util.Objects;

public class TextMessageContentEvent extends AgUiEvent {
    private final String messageId;
    private final String delta; // 增量文本

    public TextMessageContentEvent(String messageId, String delta) {
        super(AgUiEventType.TEXT_MESSAGE_CONTENT);
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
        TextMessageContentEvent that = (TextMessageContentEvent) o;
        return Objects.equals(messageId, that.messageId) && Objects.equals(delta, that.delta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, delta);
    }
}
