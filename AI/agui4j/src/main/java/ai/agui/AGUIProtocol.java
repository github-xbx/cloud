package ai.agui;


import ai.agui.enums.AGUIMessageRole;
import ai.agui.event.*;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.TokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.FluxSink;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class AGUIProtocol extends AGUIResultBase implements Consumer<FluxSink<AGUIEvent>> {

    private static final Logger logger = LoggerFactory.getLogger(AGUIProtocol.class);

    private final TokenStream tokenStream;
    private final AGUIEvent EVENT_RUN_STARTED;
    private final AGUIEvent EVENT_RUN_FINISHED;
    private AGUIEvent EVENT_RUN_ERROR;
    private final String messageId;

    public AGUIProtocol(String runId, String threadId, TokenStream tokenStream) {
        super(runId, threadId);
        this.tokenStream = tokenStream;
        this.EVENT_RUN_STARTED = new RunStartedEvent(runId, threadId);
        this.EVENT_RUN_FINISHED = new RunFinishedEvent(runId, threadId);
        messageId = UUID.randomUUID().toString();
    }

    @Override
    public void accept(FluxSink<AGUIEvent> fluxSink) {

        AtomicBoolean responseFirst = new AtomicBoolean(true);
        logger.info("==========> 开始回答 <==========");
        fluxSink.next(EVENT_RUN_STARTED); //开始
        fluxSink.next(new StateSnapshotEvent(Map.of("status", "processing", "threadId", getThreadId())));
        fluxSink.next(new ReasoningMessageStartEvent(messageId));  //思考开始

        tokenStream
                .onPartialThinking(thinking -> {
                    fluxSink.next(new ReasoningMessageContentEvent(messageId, thinking.text())); //思考内容
                })
                .onPartialResponse(response -> {
                    if (responseFirst.get()){
                        fluxSink.next(new ReasoningMessageEndEvent(messageId)); //思考结束
                        fluxSink.next(new TextMessageStartEvent(messageId, AGUIMessageRole.ASSISTANT)); //正式回答开始
                        responseFirst.set(false); //标志位 置 false
                    }
                    fluxSink.next(new TextMessageContentEvent(messageId, response)); //回答内容
                })
                .onCompleteResponse(chatResponse -> {

                    fluxSink.next(new TextMessageEndEvent(messageId)); //结束回答
                    fluxSink.next(new StateDeltaEvent(Map.of("status", "completed")));
                    fluxSink.next(EVENT_RUN_FINISHED); //结束

                    TokenUsage tokenUsage = chatResponse.metadata().tokenUsage();
                    logger.info("token 用量, 输入 token = [{}], 输出 token = [{}], 总 token = [{}]",tokenUsage.inputTokenCount(),tokenUsage.outputTokenCount(),tokenUsage.totalTokenCount());
                    AiMessage aiMessage = chatResponse.aiMessage();
                    logger.info("response text = [{}], thinking => [{}], tool => [{}], attributes => [{}]",aiMessage.text(),aiMessage.thinking(),aiMessage.toolExecutionRequests(),aiMessage.attributes());
                })
                .onError(throwable -> {
                    EVENT_RUN_ERROR = new RunErrorEvent(getRunId(),getThreadId(),throwable.getMessage());
                    fluxSink.next(EVENT_RUN_ERROR);
                    fluxSink.next(EVENT_RUN_FINISHED); //结束

                })
                .start();

    }
}
