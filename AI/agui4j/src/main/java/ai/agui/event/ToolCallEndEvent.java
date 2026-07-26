package ai.agui.event;



import ai.agui.enums.AgUiEventType;

import java.util.Objects;

public class ToolCallEndEvent extends AGUIEvent {
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
