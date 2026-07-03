package com.xbx.study.GRPC.collect.grpc.service;

import com.xbx.study.GRPC.collect.grpc.session.FileOperationsClientSession;
import com.xbx.study.GRPC.collect.manager.RpcConnectionManager;
import com.xbx.study.GRPC.collect.proto.FileClientMsg;
import com.xbx.study.GRPC.collect.proto.FileOperationsGrpc;
import com.xbx.study.GRPC.collect.proto.FileServerMsg;
import com.xbx.study.GRPC.collect.proto.Operations;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@GrpcService
public class FileOperationsService extends FileOperationsGrpc.FileOperationsImplBase {

    private static final Logger logger = LoggerFactory.getLogger(FileOperationsService.class);

    private final RpcConnectionManager manager;

    public FileOperationsService(RpcConnectionManager rpcConnectionManager) {
        this.manager = rpcConnectionManager;
    }


    @Override
    public StreamObserver<FileClientMsg> fileOperationStream(StreamObserver<FileServerMsg> responseObserver) {

        FileOperationsClientSession session = new FileOperationsClientSession(responseObserver);



        return new StreamObserver<FileClientMsg>() {
            @Override
            public void onNext(FileClientMsg fileClientMsg) {
                //处理客户端的请求
                handler(fileClientMsg, session);
            }

            @Override
            public void onError(Throwable throwable) {
                // 异常处理，清理会话
                manager.unregister(session.getSessionId());
                session.close();
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                manager.unregister(session.getSessionId());
                session.close();
                responseObserver.onCompleted();
            }
        };
    }


    private void handler(FileClientMsg fileClientMsg, FileOperationsClientSession session ){

        if (StringUtils.isEmpty(fileClientMsg.getGrpcId()) && StringUtils.isNotEmpty(fileClientMsg.getDeviceId())){
            session.onClientMessage(fileClientMsg, fileClientMsg.getDeviceId());
        }else if (StringUtils.isNotEmpty(fileClientMsg.getGrpcId())){
            session.onClientMessage(fileClientMsg, fileClientMsg.getGrpcId());
        }else {
            logger.info("客户端上送的消息有误");
            FileServerMsg msg = FileServerMsg.newBuilder().setGrpcId(UUID.randomUUID().toString()).setOperations(Operations.ERROR).build();
            session.getResponseObserver().onNext(msg);
        }

    }


}
