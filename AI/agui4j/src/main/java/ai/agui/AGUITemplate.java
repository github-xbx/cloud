package ai.agui;

import ai.agui.event.AGUIEvent;
import dev.langchain4j.service.TokenStream;
import reactor.core.publisher.Flux;

public class AGUITemplate {


    /**
     * agent 对话返回方法
     * @param
     * @return
     */
    public Flux<AGUIEvent> chat(String runID, String threadID, TokenStream tokenStream){
        AGUIProtocol aguiProtocol = new AGUIProtocol(runID, threadID, tokenStream);
        return Flux.create(aguiProtocol);
    }

}
