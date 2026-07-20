package com.xbx.study.ai.event.impl;

import com.xbx.study.ai.enums.AgUiEventType;
import com.xbx.study.ai.event.AgUiEvent;

import java.util.Objects;

public class ToolCallStartEvent extends AgUiEvent {
    private final String toolCallId;
    private final String toolName;

    public ToolCallStartEvent(String toolCallId, String toolName) {
        super(AgUiEventType.TOOL_CALL_START);
        this.toolCallId = toolCallId;
        this.toolName = toolName;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public String getToolName() {
        return toolName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ToolCallStartEvent that = (ToolCallStartEvent) o;
        return Objects.equals(toolCallId, that.toolCallId) && Objects.equals(toolName, that.toolName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(toolCallId, toolName);
    }
}
