package com.xbx.study.GRPC.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xbx.study.GRPC.collect.service.BusinessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketHandler extends TextWebSocketHandler {


    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    // 存储所有在线会话（线程安全），key 为 session.getId()
    private static final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();


    private BusinessService businessService;
    @Lazy
    public WebSocketHandler(BusinessService businessService ) {
        this.businessService = businessService;
    }



    /**
     * 连接建立成功时调用
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        logger.info("websocket 连接建立成功，session => {}",session);
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        String payload = message.getPayload();
        logger.info("websocket <UNK>payload => {}",payload);

        ClientMessage clientMessage = objectMapper.readValue(payload, ClientMessage.class);

        handlerMessageByType(session,clientMessage);

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

        logger.error("websocket connect error => {}", exception.getMessage());
    }


    public static void sendMessage(String message){

    }


    private void handlerMessageByType(WebSocketSession session, ClientMessage message) throws Exception {



        switch (message.getType()) {
            case "ping":
                session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
                break;
            case "reg":
                sessionMap.put(message.getText(), session);
                break;
            default:
                session.sendMessage(new TextMessage("{\"type\":\"message\"}"));
        }

    }




    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        logger.info("websocket pong message => {}",message);
    }




    /**
     * 给指定客户端发送消息
     */
    public void sendToSession(String sessionId, String message) {
        WebSocketSession session = sessionMap.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }





}
