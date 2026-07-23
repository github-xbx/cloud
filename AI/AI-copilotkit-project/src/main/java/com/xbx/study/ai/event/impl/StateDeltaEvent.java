package com.xbx.study.ai.event.impl;

import com.xbx.study.ai.enums.AgUiEventType;
import com.xbx.study.ai.event.AGUIEvent;

import java.util.Objects;

public class StateDeltaEvent extends AGUIEvent {
    private final Object delta; // JSON Patch 格式的状态增量

    public StateDeltaEvent(Object delta) {
        super(AgUiEventType.STATE_DELTA);
        this.delta = delta;
    }

    public Object getDelta() {
        return delta;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StateDeltaEvent that = (StateDeltaEvent) o;
        return Objects.equals(delta, that.delta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delta);
    }
}
