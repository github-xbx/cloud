package com.xbx.study.ai.graph;

import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.StateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

public class SimpleGraphApp {

    public static void main(String[] args) throws GraphStateException {
        var graph = new StateGraph<>(SimpleState.SCHEMA, SimpleState::new);
               graph.addNode("greeter", node_async(new GreeterNode()))
                .addNode("responder", node_async(new ResponderNode()))
                .addEdge(StateGraph.START, "greeter")
                .addEdge("greeter","responder")
                .addEdge("responder", StateGraph.END);


        var compiledGraph = graph.compile();

        // stream() 会依次产出每个节点执行后的章台快照
        for (var output : compiledGraph.stream(Map.of(SimpleState.message_key, "开始"))) {
            System.out.println("节点: " + output.node() + " 后的状态: " + output.state().messages());
        }
    }




}
