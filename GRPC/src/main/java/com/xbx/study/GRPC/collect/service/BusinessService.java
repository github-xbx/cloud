package com.xbx.study.GRPC.collect.service;

import com.google.protobuf.Message;
import com.xbx.study.GRPC.collect.grpc.service.FileOperationsService;
import com.xbx.study.GRPC.collect.grpc.session.ClientSession;
import com.xbx.study.GRPC.collect.proto.FileServerMsg;
import com.xbx.study.GRPC.collect.manager.RpcConnectionManager;
import com.xbx.study.GRPC.websocket.WebSocketHandler;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class BusinessService {


    private final WebSocketHandler webSocketHandler;
    private final FileOperationsService  fileOperationsService;
    private final RpcConnectionManager  manager;

    @Lazy
    public BusinessService(WebSocketHandler webSocketHandler, FileOperationsService fileOperationsService, RpcConnectionManager rpcConnectionManager) {
        this.webSocketHandler = webSocketHandler;
        this.fileOperationsService = fileOperationsService;
        this.manager = rpcConnectionManager;
    }



    public void sendGrpcClient(String deviceId){

        ClientSession<? extends Message, ? extends Message> session =  manager.getSession(deviceId);

        FileServerMsg msg = FileServerMsg.newBuilder()
                .setGrpcId("123")
                .setIsDirectory(true)
                .build();
        session.sendMsgAndWait(msg);

    }












}
