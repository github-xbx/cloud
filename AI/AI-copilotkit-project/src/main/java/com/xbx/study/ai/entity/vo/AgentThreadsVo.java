package com.xbx.study.ai.entity.vo;

import com.xbx.study.ai.entity.dto.AgentThread;

import java.util.List;

public class AgentThreadsVo {
    private List<AgentThread> threads;
    private String joinCode;
    private String nextCursor;

    public List<AgentThread> getThreads() {
        return threads;
    }

    public void setThreads(List<AgentThread> threads) {
        this.threads = threads;
    }

    public String getJoinCode() {
        return joinCode;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }
}
