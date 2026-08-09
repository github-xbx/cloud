package ai.agui;

import ai.agui.common.OriginalMessage;
import ai.agui.event.AGUIEvent;
import dev.langchain4j.service.TokenStream;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

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



    public Flux<AGUIEvent> stream(Map<String, List<OriginalMessage>> messages){

        return null;
    }



}
