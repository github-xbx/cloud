package com.xbx.study.ai.event.impl;

import com.xbx.study.ai.enums.AgUiEventType;
import com.xbx.study.ai.event.AgUiEvent;

import java.util.Objects;

public class ToolCallArgsEvent extends AgUiEvent {
    private final String toolCallId;
    private final String args; // JSON 格式的参数

    public ToolCallArgsEvent(String toolCallId, String args) {
        super(AgUiEventType.TOOL_CALL_ARGS);
        this.toolCallId = toolCallId;
        this.args = args;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public String getArgs() {
        return args;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ToolCallArgsEvent that = (ToolCallArgsEvent) o;
        return Objects.equals(toolCallId, that.toolCallId) && Objects.equals(args, that.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(toolCallId, args);
    }
}
