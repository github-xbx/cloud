package com.xbx.study.ai.service;

import com.xbx.study.ai.dto.AgentMessage;
import com.xbx.study.ai.dto.RunAgentInput;
import com.xbx.study.ai.event.AgUiEvent;
import com.xbx.study.ai.event.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class AgentService {
    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);


    /**
     * 执行agent 逻辑 通过时间消费发射 AG-UI 事件
     */
    public void execute(RunAgentInput input, Consumer<AgUiEvent> eventEmitter){
        String runId = input.getRunId();
        String threadId = input.getThreadId();

        logger.info("Starting agent run: runId={}, threadId={}", runId, threadId);




        try {
            // 1. 发射 RUN_STARTED 事件
            eventEmitter.accept(new RunStartedEvent(runId, threadId));

            // 2. 可选：发射初始状态快照
            eventEmitter.accept(new StateSnapshotEvent(
                    Map.of("status", "processing", "threadId", threadId)
            ));

            // 3. 获取用户输入
            String userInput = input.getMessages().stream()
                    .filter(m -> "user".equals(m.getRole()))
                    .map(AgentMessage::getContent)
                    .reduce((a, b) -> b) //获取最后一个元素
                    .orElse("你好");

            // 4. 模拟推理过程 (REASONING 事件)
            String reasoningId = UUID.randomUUID().toString();
            eventEmitter.accept(new ReasoningMessageStartEvent(reasoningId, "reasoning"));

            String[] reasoningParts = {"正在思考", "分析问题", "准备回答"};
            for (String part : reasoningParts) {
                Thread.sleep(200); // 模拟推理延迟
                eventEmitter.accept(new ReasoningMessageContentEvent(reasoningId, part + "... "));
            }
            eventEmitter.accept(new ReasoningMessageEndEvent(reasoningId));

            // 5. 模拟工具调用 (TOOL_CALL 事件)
            String toolCallId = UUID.randomUUID().toString();
            eventEmitter.accept(new ToolCallStartEvent(toolCallId, "search_knowledge"));
            eventEmitter.accept(new ToolCallArgsEvent(toolCallId, "{\"query\": \"" + userInput + "\"}"));
            Thread.sleep(300);
            eventEmitter.accept(new ToolCallEndEvent(toolCallId));
            eventEmitter.accept(new ToolCallResultEvent(toolCallId, "找到了相关信息...",reasoningId));

            // 6. 生成最终回复 (TEXT_MESSAGE 事件)
            String messageId = UUID.randomUUID().toString();
            eventEmitter.accept(new TextMessageStartEvent(messageId, "assistant"));

            String response = generateResponse(userInput);
            // 模拟流式输出（逐字）
            for (char c : response.toCharArray()) {
                eventEmitter.accept(new TextMessageContentEvent(messageId, String.valueOf(c)));
                Thread.sleep(30); // 模拟打字机效果
            }
            eventEmitter.accept(new TextMessageEndEvent(messageId));

            // 7. 可选：发射状态增量
            eventEmitter.accept(new StateDeltaEvent(
                    Map.of("status", "completed")
            ));

            // 8. 发射 RUN_FINISHED 事件
            eventEmitter.accept(new RunFinishedEvent(runId, threadId));

            logger.info("Agent run completed: runId={}", runId);

        } catch (Exception e) {
            logger.error("Agent run failed: runId={}", runId, e);
            eventEmitter.accept(new RunErrorEvent(runId, e.getMessage(), e.getClass().getName()));
        }



    }



    private String generateResponse(String userInput) {
        // 这里可以替换为真实的 AI 模型调用
        return "你好！你问的是：「" + userInput + "」\n\n" +
                "这是一个通过 AG-UI 协议流式返回的回复。\n" +
                "AG-UI 支持文本、推理过程、工具调用等多种事件类型。";
    }


}
