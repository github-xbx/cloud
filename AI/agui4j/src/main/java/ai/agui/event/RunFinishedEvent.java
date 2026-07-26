package ai.agui.event;



import ai.agui.enums.AgUiEventType;

import java.util.Objects;

public class RunFinishedEvent extends AGUIEvent {
    private final String runId;
    private final String threadId;

    public RunFinishedEvent(String runId, String threadId) {
        super(AgUiEventType.RUN_FINISHED);
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
        RunFinishedEvent that = (RunFinishedEvent) o;
        return Objects.equals(runId, that.runId) && Objects.equals(threadId, that.threadId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(runId, threadId);
    }
}
