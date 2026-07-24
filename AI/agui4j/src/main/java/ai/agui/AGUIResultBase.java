package ai.agui;

public abstract class AGUIResultBase {
    private final String runId;
    private final String threadId;

    public AGUIResultBase(String runId, String threadId){
        this.runId = runId;
        this.threadId = threadId;
    }

    public String getRunId() {
        return runId;
    }

    public String getThreadId() {
        return threadId;
    }
}
