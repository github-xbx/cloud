package com.xbx.study.GRPC.collect.grpc.session;

import com.google.protobuf.Message;
import io.grpc.stub.StreamObserver;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public abstract class RpcClientSession<REQ extends Message,RESP extends Message> implements ClientSession<REQ,RESP> {

    private  String sessionId;
    private  StreamObserver<RESP> responseObserver;
    // 线程安全地存储待处理的请求
    private final Map<String, CompletableFuture<REQ>> futureMap = new ConcurrentHashMap<>();


    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public StreamObserver<RESP> getResponseObserver() {
        return responseObserver;
    }

    public void setResponseObserver(StreamObserver<RESP> responseObserver) {
        this.responseObserver = responseObserver;
    }

    @Override
    public CompletableFuture<REQ> sendMsgAndWait(RESP msg, String connectionId) {

        CompletableFuture<REQ> future = new CompletableFuture<>();
        futureMap.put(connectionId, future);

        responseObserver.onNext(msg);

        //等待 5秒 如果超时
        future.orTimeout(120, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    futureMap.remove(connectionId);
                    return null;
                });


        return future;
    }


    @Override
    public void onClientMessage(REQ msg, String connectionId) {
        CompletableFuture<REQ> future = futureMap.get(connectionId);
        if (future != null){
            //如果是client 响应 server 的消息直接处理
            future.complete(msg);
        }else {
            //client 旨在第一次建立连接时上送保存 连接id 设备唯一
            this.sessionId = connectionId;
        }
    }

    // 关闭会话，清理资源
    public void close() {
        futureMap.values().forEach(f -> f.cancel(true));
        futureMap.clear();
    }
}
