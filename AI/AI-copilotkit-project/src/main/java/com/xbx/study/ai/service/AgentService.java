package com.xbx.study.ai.service;

import ai.agui.AGUITemplate;
import ai.agui.common.OriginalMessage;
import ai.agui.event.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xbx.study.ai.entity.dto.AgentMessage;
import com.xbx.study.ai.entity.dto.AgentThread;
import com.xbx.study.ai.entity.dto.RunAgentInput;
import com.xbx.study.ai.entity.po.AiMessagePo;
import com.xbx.study.ai.entity.po.AiThreadPo;
import com.xbx.study.ai.mapper.AiMessageMapper;
import com.xbx.study.ai.mapper.AiThreadMapper;
import com.xbx.study.ai.service.model.QwenChatAssistant;
import dev.langchain4j.invocation.InvocationParameters;
import dev.langchain4j.service.TokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AgentService {
    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);

    private static final String KEY_STATUS = "status";
    private static final String KEY_THREAD_ID = "threadId";
    private static final String VALUE_STATUS_PROCESSING = "processing";
    private static final String VALUE_STATUS_COMPLETED = "completed";

    private static final Integer PAGE_SIZE = 5;

    private final QwenChatAssistant streamingChatAssistant;
    private final AiThreadMapper aiThreadMapper;
    private final AiMessageMapper aiMessageMapper;



    public AgentService(QwenChatAssistant streamingChatAssistant, AiThreadMapper aiThreadMapper, AiMessageMapper aiMessageMapper) {
        this.streamingChatAssistant = streamingChatAssistant;
        this.aiThreadMapper = aiThreadMapper;
        this.aiMessageMapper = aiMessageMapper;
    }




    @Transactional
    public PageInfo<AiThreadPo> threadList(Integer pageNum){
        //pageNum = pageNum != null ? pageNum : 1;
        PageHelper.startPage(pageNum,PAGE_SIZE);
        List<AiThreadPo> aiThreadPos = aiThreadMapper.selectAll();
        return new PageInfo<>(aiThreadPos);
    }


    @Transactional(readOnly = true)
    public Flux<AGUIEvent> historyMessages(String threadId){
        if (threadId == null || threadId.isEmpty()){
            return Flux.empty();
        }
        List<AiMessagePo> list = this.aiMessageMapper.selectByThreadId(threadId);

        Map<String, List<OriginalMessage>> map = list.stream().map(msg -> {
            return new OriginalMessage(msg.getRole(), msg.getContent(), msg.getRunId(), msg.getThreadId(), msg.getMessageId(), msg.getCreateAt());
        }).collect(Collectors.groupingBy(OriginalMessage::runId));

        return new AGUITemplate().stream(map);
    }




    /**
     * 执行agent 逻辑 通过时间消费发射 AG-UI 事件
     */
//    public void execute(RunAgentInput input, Consumer<AGUIEvent> eventEmitter){
//        String runId = input.getRunId();
//        String threadId = input.getThreadId();
//
//        logger.info("Starting agent run: runId={}, threadId={}", runId, threadId);
//
//
//
//
//        try {
//            // 1. 发射 RUN_STARTED 事件
//            eventEmitter.accept(new RunStartedEvent(runId, threadId));
//
//            // 2. 可选：发射初始状态快照
//            eventEmitter.accept(new StateSnapshotEvent(
//                    Map.of("status", "processing", "threadId", threadId)
//            ));
//
//            // 3. 获取用户输入
//            String userInput = input.getMessages().stream()
//                    .filter(m -> "user".equals(m.getRole()))
//                    .map(AgentMessage::getContent)
//                    .reduce((a, b) -> b) //获取最后一个元素
//                    .orElse("你好");
//
//            // 4. 模拟推理过程 (REASONING 事件)
//            String reasoningId = UUID.randomUUID().toString();
//            eventEmitter.accept(new ReasoningMessageStartEvent(reasoningId));
//
//            String[] reasoningParts = {"正在思考", "分析问题", "准备回答"};
//            for (String part : reasoningParts) {
//                Thread.sleep(200); // 模拟推理延迟
//                eventEmitter.accept(new ReasoningMessageContentEvent(reasoningId, part + "... "));
//            }
//            eventEmitter.accept(new ReasoningMessageEndEvent(reasoningId));
//
//            // 5. 模拟工具调用 (TOOL_CALL 事件)
//            String toolCallId = UUID.randomUUID().toString();
//            eventEmitter.accept(new ToolCallStartEvent(toolCallId, "search_knowledge"));
//            eventEmitter.accept(new ToolCallArgsEvent(toolCallId, "{\"query\": \"" + userInput + "\"}"));
//            Thread.sleep(300);
//            eventEmitter.accept(new ToolCallEndEvent(toolCallId));
//            eventEmitter.accept(new ToolCallResultEvent(toolCallId, "找到了相关信息...",reasoningId));
//
//            // 6. 生成最终回复 (TEXT_MESSAGE 事件)
//            String messageId = UUID.randomUUID().toString();
//            eventEmitter.accept(new TextMessageStartEvent(messageId, "assistant"));
//
//            String response = generateResponse(userInput);
//            // 模拟流式输出（逐字）
//            for (char c : response.toCharArray()) {
//                eventEmitter.accept(new TextMessageContentEvent(messageId, String.valueOf(c)));
//                Thread.sleep(30); // 模拟打字机效果
//            }
//            eventEmitter.accept(new TextMessageEndEvent(messageId));
//
//            // 7. 可选：发射状态增量
//            eventEmitter.accept(new StateDeltaEvent(
//                    Map.of("status", "completed")
//            ));
//
//            // 8. 发射 RUN_FINISHED 事件
//            eventEmitter.accept(new RunFinishedEvent(runId, threadId));
//
//            logger.info("Agent run completed: runId={}", runId);
//
//        } catch (Exception e) {
//            logger.error("Agent run failed: runId={}", runId, e);
//            eventEmitter.accept(new RunErrorEvent(runId, e.getMessage(), e.getClass().getName()));
//        }
//
//
//
//    }


    @Transactional(rollbackFor = Exception.class)
    public void saveDb(String threadId, String runId,AgentMessage agentMessage){
        LocalDateTime nowTime = LocalDateTime.now();
        AiThreadPo aiThreadPo = aiThreadMapper.selectByThreadId(threadId);
        if (aiThreadPo == null){
            //新增
            aiThreadPo = new AiThreadPo();
            aiThreadPo.setThreadId(threadId);
            aiThreadPo.setThreadName(agentMessage.getContent());
            aiThreadPo.setCreateAt(nowTime);
            aiThreadPo.setUpdateAt(nowTime);
            aiThreadPo.setLastRunAt(nowTime);
            aiThreadMapper.insert(aiThreadPo);
        }else {
            //修改
            aiThreadMapper.updateTimeByThreadId(threadId, nowTime);
        }

        //保存消息 用户提问的消息
        AiMessagePo aiMessagePo = new AiMessagePo(agentMessage.getId(), threadId, runId, agentMessage.getRole(), agentMessage.getContent(), nowTime);
        aiMessageMapper.insert(aiMessagePo);


    }


    /**
     * 处理啊
     * @param input
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Flux<AGUIEvent> execute(RunAgentInput input){
        String runId = input.getRunId();
        String threadId = input.getThreadId();
        String textId = UUID.randomUUID().toString();
        String thinkId = UUID.randomUUID().toString();

        // 3. 获取用户输入
        AgentMessage agentMessage = input.getMessages().stream()
                .filter(m -> "user".equals(m.getRole()))
                .reduce((a, b) -> b) //获取最后一个元素
                .orElseThrow(()  -> new RuntimeException("参数错误，没有获取到用户输入的信息"));

        saveDb(threadId,runId,agentMessage);

        InvocationParameters parameters = new InvocationParameters();
        parameters.put("threadId", threadId);
        parameters.put("runId", runId);
        parameters.put("textId", textId);
        parameters.put("thinkingId", thinkId);

        TokenStream chatResponse = streamingChatAssistant.chat(agentMessage.getContent(), parameters);

        return  new AGUITemplate().chat(runId, threadId, chatResponse, textId, thinkId);
    }


}
