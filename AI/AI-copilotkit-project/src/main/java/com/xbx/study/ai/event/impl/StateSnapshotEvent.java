package com.xbx.study.ai.event.impl;

import com.xbx.study.ai.enums.AgUiEventType;
import com.xbx.study.ai.event.AGUIEvent;

import java.util.Objects;

public class StateSnapshotEvent extends AGUIEvent {
    private final Object state; // 完整状态快照

    public StateSnapshotEvent(Object state) {
        super(AgUiEventType.STATE_SNAPSHOT);
        this.state = state;
    }

    public Object getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StateSnapshotEvent that = (StateSnapshotEvent) o;
        return Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state);
    }
}
