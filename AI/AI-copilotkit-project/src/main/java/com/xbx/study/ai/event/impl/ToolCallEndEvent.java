package com.xbx.study.ai.event.impl;

import com.xbx.study.ai.enums.AgUiEventType;
import com.xbx.study.ai.event.AgUiEvent;

import java.util.Objects;

public class ToolCallEndEvent extends AgUiEvent {
    private final String toolCallId;

    public ToolCallEndEvent(String toolCallId) {
        super(AgUiEventType.TOOL_CALL_END);
        this.toolCallId = toolCallId;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ToolCallEndEvent that = (ToolCallEndEvent) o;
        return Objects.equals(toolCallId, that.toolCallId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(toolCallId);
    }
}
