package com.xbx.study.ai.event;

import com.xbx.study.ai.enums.AgUiEventType;

//@JsonTypeInfo(
//        use = JsonTypeInfo.Id.NAME,
//        property = "type",
//        include = JsonTypeInfo.As.EXISTING_PROPERTY
//)
//@JsonSubTypes({
//        @JsonSubTypes.Type(value = RunStartedEvent.class, name = "RUN_STARTED"),
//        @JsonSubTypes.Type(value = RunFinishedEvent.class, name = "RUN_FINISHED"),
//        @JsonSubTypes.Type(value = RunErrorEvent.class, name = "RUN_ERROR"),
//        @JsonSubTypes.Type(value = TextMessageStartEvent.class, name = "TEXT_MESSAGE_START"),
//        @JsonSubTypes.Type(value = TextMessageContentEvent.class, name = "TEXT_MESSAGE_CONTENT"),
//        @JsonSubTypes.Type(value = TextMessageEndEvent.class, name = "TEXT_MESSAGE_END"),
//        @JsonSubTypes.Type(value = ReasoningMessageStartEvent.class, name = "REASONING_MESSAGE_START"),
//        @JsonSubTypes.Type(value = ReasoningMessageContentEvent.class, name = "REASONING_MESSAGE_CONTENT"),
//        @JsonSubTypes.Type(value = ReasoningMessageEndEvent.class, name = "REASONING_MESSAGE_END"),
//        @JsonSubTypes.Type(value = ToolCallStartEvent.class, name = "TOOL_CALL_START"),
//        @JsonSubTypes.Type(value = ToolCallArgsEvent.class, name = "TOOL_CALL_ARGS"),
//        @JsonSubTypes.Type(value = ToolCallEndEvent.class, name = "TOOL_CALL_END"),
//        @JsonSubTypes.Type(value = ToolCallResultEvent.class, name = "TOOL_CALL_RESULT"),
//        @JsonSubTypes.Type(value = StateSnapshotEvent.class, name = "STATE_SNAPSHOT"),
//        @JsonSubTypes.Type(value = StateDeltaEvent.class, name = "STATE_DELTA")
//})
public abstract class AGUIEvent {

    private final AgUiEventType type;

    protected AGUIEvent(AgUiEventType type){
        this.type = type;
    }

    public AgUiEventType getType(){
        return type;
    }

}
