package com.xbx.study.ai.enums;

/**
 * AG-UI 协议所有事件类型
 */
public enum AgUiEventType {


    // 生命周期事件
    RUN_STARTED,
    RUN_FINISHED,
    RUN_ERROR,

    // 文本消息事件
    TEXT_MESSAGE_START,
    TEXT_MESSAGE_CONTENT,
    TEXT_MESSAGE_END,

    // 推理/思考事件
    REASONING_MESSAGE_START,
    REASONING_MESSAGE_CONTENT,
    REASONING_MESSAGE_END,

    // 工具调用事件
    TOOL_CALL_START,
    TOOL_CALL_ARGS,
    TOOL_CALL_END,
    TOOL_CALL_RESULT,

    // 状态同步事件
    STATE_SNAPSHOT,
    STATE_DELTA
}
