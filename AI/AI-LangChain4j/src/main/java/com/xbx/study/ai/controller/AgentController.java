package com.xbx.study.ai.controller;

import dev.langchain4j.data.message.UserMessage;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.agentexecutor.AgentExecutor;
import org.bsc.langgraph4j.streaming.StreamingOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("agent")
public class AgentController {

    private static final Logger logger = LoggerFactory.getLogger(AgentController.class);

    private final CompiledGraph<AgentExecutor.State> syncGraph;
    private final CompiledGraph<AgentExecutor.State> streamGraph;


    public AgentController(
            @Qualifier("supportAgentGraph") CompiledGraph<AgentExecutor.State> syncGraph,
            @Qualifier("supportAgentStreamGraph") CompiledGraph<AgentExecutor.State> streamGraph) {
        this.syncGraph = syncGraph;
        this.streamGraph = streamGraph;
    }


    /**
     * 同步问答。threadId 就是"会话 id",同一个 threadId 会记住上下文和已执行过的步骤。
     * 例:/agent/chat?threadId=u1&message=订单SO20260101001到哪了
     */
    @GetMapping("chat")
    public String chat(@RequestParam(name = "threadId", defaultValue = "default")String threadId, @RequestParam(name = "message") String message){

        RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();

        return syncGraph.invoke(GraphInput.args(Map.of("messages", UserMessage.from(message))),config)
                .flatMap(AgentExecutor.State::finalResponse) // 图的最终回答
                .orElse("(agent 没有产出最终的回答)");
    }



    @GetMapping(value = "chat/stream", produces = "text/event-stream;chatset=UTF-8")
    public Flux<String> chatStream(@RequestParam(name = "threadId", defaultValue = "default")String threadId, @RequestParam(name = "message") String message){

        var config = RunnableConfig.builder().threadId(threadId).build();
        var inputs = Map.<String ,Object>of("messages", UserMessage.from(message));

        return Flux.create(sink -> {

            try {
                for (var output : streamGraph.stream(GraphInput.args(inputs), config)) {
                    if (output instanceof StreamingOutput<?> streaming){
                        sink.next(((StreamingOutput<AgentExecutor.State>) output).chunk()); //模型突出的 token
                    }else {
                        logger.debug("node={}, state={}",output.node(),output.state());
                        //走到END 说明这一轮结束
                        if (output.isEND()){
                            output.state().finalResponse().ifPresent(r -> sink.next("\n[完成]"+r));
                        }

                    }
                }
                sink.complete();
            }catch (Throwable t){
                logger.error("agent 流式执行失败",t);
                sink.error(t);
            }

        });
    }


    /**
     * 多轮记忆演示:同一个 threadId 连续两问,第二问能记住第一问的订单号
     */
    @GetMapping("chat/multi-turn")
    public String multiTurn(@RequestParam(name = "threadId", defaultValue = "mem-demo") String threadId){
        logger.debug("threadId=>{}",threadId);
        RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();

        GraphInput firestInput = GraphInput.args(Map.of("messages", UserMessage.from("帮我查一下订单 SO20260101001")));

        String first = syncGraph.invoke(firestInput, config).flatMap(AgentExecutor.State::finalResponse).orElse("");

        GraphInput secondInput = GraphInput.args(Map.of("messages", UserMessage.from("帮我把他退了把，不想要了")));

        String second = syncGraph.invoke(secondInput, config).flatMap(AgentExecutor.State::finalResponse).orElse("");

        return "第一轮：" + first + "\n\n第二轮（省略订单号，靠记忆）："+second;
    }











}
