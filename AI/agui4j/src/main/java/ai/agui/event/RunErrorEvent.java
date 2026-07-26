package ai.agui.event;



import ai.agui.enums.AgUiEventType;

import java.util.Objects;

public class RunErrorEvent extends AGUIEvent {
    private final String runId;
    private final String message;
    private final String stack;

    public RunErrorEvent(String runId, String message, String stack) {
        super(AgUiEventType.RUN_ERROR);
        this.runId = runId;
        this.message = message;
        this.stack = stack;
    }

    public String getRunId() {
        return runId;
    }

    public String getMessage() {
        return message;
    }

    public String getStack() {
        return stack;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RunErrorEvent that = (RunErrorEvent) o;
        return Objects.equals(runId, that.runId) && Objects.equals(message, that.message) && Objects.equals(stack, that.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(runId, message, stack);
    }
}
