package com.xbx.study.ai.event.impl;

import com.xbx.study.ai.enums.AgUiEventType;
import com.xbx.study.ai.event.AgUiEvent;

import java.util.Objects;

public class ToolCallStartEvent extends AgUiEvent {
    private final String toolCallId;
    private final String toolCallName;

    public ToolCallStartEvent(String toolCallId, String toolCallName) {
        super(AgUiEventType.TOOL_CALL_START);
        this.toolCallId = toolCallId;
        this.toolCallName = toolCallName;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public String getToolCallName() {
        return toolCallName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ToolCallStartEvent that = (ToolCallStartEvent) o;
        return Objects.equals(toolCallId, that.toolCallId) && Objects.equals(toolCallName, that.toolCallName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(toolCallId, toolCallName);
    }
}
