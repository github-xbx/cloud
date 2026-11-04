package com.xbx.study.ai.websocket;

import com.xbx.study.ai.service.BaseAiChat;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiWebSocketHandler extends TextWebSocketHandler {

    private final BaseAiChat  baseAiChat;

    // 用于存储每个 WebSocket 会话对应的 Flux 订阅，以便在连接关闭时取消
    private final Map<String, Disposable> sessionSubscriptions = new ConcurrentHashMap<>();

    public AiWebSocketHandler(BaseAiChat baseAiChat) {
        this.baseAiChat = baseAiChat;
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userMessage = message.getPayload();

        //1. 调用 AI Service， 获得流式响应的 Flux<String>

        Flux<String> tokenStream = baseAiChat.stream(userMessage);

        //2.关键：将 Flux 桥接为 Sinks.Many, 以便命令的 WebSocket 处理器中消费
        // 使用 unicast 是因为每个WebSocket 连接对应一个独立的 AI 响应流
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        //3. 订阅 AI 的 Flux，并将数据、完成、错误信号发到Sink
        Disposable disposable = tokenStream.subscribe(
                token -> sink.tryEmitNext(token), // 收到一个 token
                error -> {
                    sink.tryEmitError(error);  // AI 流发生错误
                    //可选 通知前端发生了错误
                    try {
                        if (session.isOpen()){
                            session.sendMessage(new TextMessage("[ERROR]"+error.getMessage()));
                        }
                    } catch (IOException e) {
                        // 日志记录
                    }
                },
                () -> sink.tryEmitComplete()   //AI 流正常完成
        );

        //4. 管理订阅的生命周期
        sessionSubscriptions.put(session.getId(), disposable);

        //5.订阅 Sink 的Flux，并将每个token 发送到 WebSocket 客户端
        //这个订阅是阻塞试的 它在当前处理消息的线程上运行
        sink.asFlux().subscribe(
                token -> {
                    try {
                        if (session.isOpen()){
                            session.sendMessage(new TextMessage(token));
                        }
                    }catch (IOException e){

                    }
                }
        );

    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

        //6. 连接关闭时候，务必取消订阅，防止资源泄露
        Disposable disposable = sessionSubscriptions.remove(session.getId());
        if (disposable != null && !disposable.isDisposed()){
            disposable.dispose();
        }
        super.afterConnectionClosed(session,status);
    }
}
