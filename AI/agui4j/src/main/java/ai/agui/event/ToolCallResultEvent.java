package ai.agui.event;



import ai.agui.enums.AgUiEventType;

import java.util.Objects;

public class ToolCallResultEvent extends AGUIEvent {
    private final String toolCallId;
    private final String content; // 工具执行结果
    private final String messageId;

    public ToolCallResultEvent(String toolCallId, String content, String messageId) {
        super(AgUiEventType.TOOL_CALL_RESULT);
        this.toolCallId = toolCallId;
        this.content = content;
        this.messageId = messageId;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public String getContent() {
        return content;
    }

    public String getMessageId() {
        return messageId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ToolCallResultEvent that = (ToolCallResultEvent) o;
        return Objects.equals(toolCallId, that.toolCallId) && Objects.equals(content, that.content) && Objects.equals(messageId, that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(toolCallId, content, messageId);
    }
}
