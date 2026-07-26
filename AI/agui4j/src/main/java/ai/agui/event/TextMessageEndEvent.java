package ai.agui.event;



import ai.agui.enums.AgUiEventType;

import java.util.Objects;

public class TextMessageEndEvent extends AGUIEvent {
    private final String messageId;

    public TextMessageEndEvent(String messageId) {
        super(AgUiEventType.TEXT_MESSAGE_END);
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TextMessageEndEvent that = (TextMessageEndEvent) o;
        return Objects.equals(messageId, that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }
}
