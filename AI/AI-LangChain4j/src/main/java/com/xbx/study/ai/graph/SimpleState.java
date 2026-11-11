package com.xbx.study.ai.graph;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 1. 定义状态
 * 这里使用 Channels.appender() 作为 Reducer，使 messages 字段成为一个累加列表。
 */
public class SimpleState extends AgentState {

    public static final String message_key = "message";

    // Schema 定义 messages 字段使用追加模式
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            message_key, Channels.appender(ArrayList::new)
    );

    public SimpleState(Map<String, Object> initData) {
        super(initData);
    }

    public List<String> messages(){
        return this.<List<String>>value(message_key).orElse(new ArrayList<>());
    }
}
