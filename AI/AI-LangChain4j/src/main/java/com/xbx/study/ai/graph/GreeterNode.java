package com.xbx.study.ai.graph;

import org.bsc.langgraph4j.action.NodeAction;

import java.util.Map;

class GreeterNode implements NodeAction<SimpleState> {

    @Override
    public Map<String, Object> apply(SimpleState simpleState) throws Exception {
        System.out.println("GreeterNode 执行，当前消息: " + simpleState.messages());
        return Map.of(SimpleState.message_key, "Hello World");
    }



}

class ResponderNode implements NodeAction<SimpleState> {
    @Override
    public Map<String, Object> apply(SimpleState simpleState) throws Exception {
        System.out.println("ResponderNode 执行，当前消息: " + simpleState.messages());
        return Map.of(SimpleState.message_key, "Acknowledged.");

    }
}