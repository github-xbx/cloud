package com.xbx.study.GRPC.collect.manager;

import com.xbx.study.GRPC.collect.common.proto.ServerMessage;
import com.xbx.study.GRPC.collect.proto.FileClientMsg;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RpcConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(RpcConnectionManager.class);

    private final Map<String, StreamObserver<ServerMessage>> rpcClient = new ConcurrentHashMap<>();


    public void register(String deviceId, StreamObserver<ServerMessage> streamObserver) {
        rpcClient.put(deviceId, streamObserver);
        logger.info("device register : {}", deviceId);
    }

    public void unregister(String deviceId) {
        rpcClient.remove(deviceId);
        logger.info("device unregister : {}", deviceId);
    }

    /**
     * 给指定的客户端发送消息
     * @param deviceId 客户端id
     * @param message 消息
     * @return
     */
    public boolean sendMessage(String deviceId, ServerMessage message) {

        StreamObserver<ServerMessage> streamObserver = rpcClient.get(deviceId);
        if (streamObserver == null) {
            return false;
        }
        try {
            streamObserver.onNext(message);
            return true;
        } catch (Exception e) {
            rpcClient.remove(deviceId);
            return false;
        }

    }



}
