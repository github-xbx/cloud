package ai.agui.event;



import ai.agui.enums.AgUiEventType;

import java.util.Objects;

public class RunStartedEvent extends AGUIEvent {
    private final String runId;
    private final String threadId;

    public RunStartedEvent(String runId, String threadId) {
        super(AgUiEventType.RUN_STARTED);
        this.runId = runId;
        this.threadId = threadId;
    }

    public String getRunId() {
        return runId;
    }

    public String getThreadId() {
        return threadId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RunStartedEvent that = (RunStartedEvent) o;
        return Objects.equals(runId, that.runId) && Objects.equals(threadId, that.threadId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(runId, threadId);
    }
}
