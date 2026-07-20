package com.xbx.study.ai.dto;

import java.util.List;
import java.util.Map;


public class RunAgentInput {

    private String threadId;
    private String runId;
    private List<AgentMessage> messages;
    private Map<String, Object> state;
    private List<String> context;

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public List<AgentMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<AgentMessage> messages) {
        this.messages = messages;
    }

    public Map<String, Object> getState() {
        return state;
    }

    public void setState(Map<String, Object> state) {
        this.state = state;
    }

    public List<String> getContext() {
        return context;
    }

    public void setContext(List<String> context) {
        this.context = context;
    }
}
