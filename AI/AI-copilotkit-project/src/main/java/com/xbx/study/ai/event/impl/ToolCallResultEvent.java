package com.xbx.study.ai.event.impl;

import com.xbx.study.ai.enums.AgUiEventType;
import com.xbx.study.ai.event.AgUiEvent;

import java.util.Objects;

public class ToolCallResultEvent extends AgUiEvent {
    private final String toolCallId;
    private final String result; // 工具执行结果

    public ToolCallResultEvent(String toolCallId, String result) {
        super(AgUiEventType.TOOL_CALL_RESULT);
        this.toolCallId = toolCallId;
        this.result = result;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public String getResult() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ToolCallResultEvent that = (ToolCallResultEvent) o;
        return Objects.equals(toolCallId, that.toolCallId) && Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(toolCallId, result);
    }
}
