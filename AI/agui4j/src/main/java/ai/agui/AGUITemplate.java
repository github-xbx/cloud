package ai.agui;

import ai.agui.common.OriginalMessage;
import ai.agui.enums.AGUIMessageRole;
import ai.agui.event.*;
import dev.langchain4j.service.TokenStream;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AGUITemplate {


    /**
     * agent 对话返回方法
     * @param
     * @return
     */
    public Flux<AGUIEvent> chat(String runID, String threadID, TokenStream tokenStream, String textMessageId, String reasoningMessageId){
        AGUIProtocolConsumer aguiProtocol = new  AGUIProtocolConsumer(runID, threadID, tokenStream, textMessageId, reasoningMessageId);
        return Flux.create(aguiProtocol);
    }


    /**
     * 消息 转为 Flux
     * @param map
     * @return
     */
    public Flux<AGUIEvent> stream(Map<String, List<OriginalMessage>> map){

        // 再按每组中最新消息的时间（即列表第一条）对 entry 排序
        LinkedHashMap<String, List<OriginalMessage>> linkedHashMap = map.entrySet().stream()
                .sorted((e1, e2) -> {
                    LocalDateTime t1 = e1.getValue().isEmpty() ? null : e1.getValue().getFirst().time();
                    LocalDateTime t2 = e2.getValue().isEmpty() ? null : e2.getValue().getFirst().time();
                    if (t1 == null && t2 == null) return 0;
                    if (t1 == null) return 1;
                    if (t2 == null) return -1;
                    return t1.compareTo(t2); // 降序
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (old, newv) -> old,
                        LinkedHashMap::new));



        return Flux.fromIterable(linkedHashMap.entrySet())
                .flatMap(entry -> {
                    String runId = entry.getKey();
                    List<OriginalMessage> list = entry.getValue();
                    List<AGUIEvent> aguiEventList = new ArrayList<>();
                    for (OriginalMessage message : list) {
                        RunStartedEvent runStartedEvent = new RunStartedEvent(runId, message.threadId());
                        aguiEventList.add(runStartedEvent);

                        if (AGUIMessageRole.USER.equals(message.role()) || AGUIMessageRole.ASSISTANT.equals(message.role())) {
                            aguiEventList.add(new TextMessageStartEvent(message.messageId(), message.role()));
                            aguiEventList.add(new TextMessageContentEvent(message.messageId(), message.message()));
                            aguiEventList.add(new TextMessageEndEvent(message.messageId()));
                        }
                        if (AGUIMessageRole.REASONING.equals(message.role())) {
                            aguiEventList.add(new ReasoningMessageStartEvent(message.messageId()));
                            aguiEventList.add(new ReasoningMessageContentEvent(message.messageId(), message.message()));
                            aguiEventList.add(new ReasoningMessageEndEvent(message.messageId()));
                        }

                        RunFinishedEvent runFinishedEvent = new RunFinishedEvent(runId, message.threadId());
                        aguiEventList.add(runFinishedEvent);
                    }

                    return Flux.fromIterable(aguiEventList);
                });

    }



}
